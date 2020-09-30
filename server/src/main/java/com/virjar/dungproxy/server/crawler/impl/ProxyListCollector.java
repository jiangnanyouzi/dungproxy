package com.virjar.dungproxy.server.crawler.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.virjar.dungproxy.client.httpclient.HttpInvoker;
import com.virjar.dungproxy.server.crawler.NewCollector;
import com.virjar.dungproxy.server.entity.Proxy;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by virjar on 16/11/26.
 */
@Component
public class ProxyListCollector extends NewCollector {
    private static final Logger logger = LoggerFactory.getLogger(ProxyListCollector.class);
    private static final Pattern ipAndPortPattern = Pattern.compile(
            "(([1-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}):(\\d+)");

    public ProxyListCollector() {
        setDuration(24 * 60);
    }

    @Override
    public String lasUrl() {
        return "https://www.proxy-list.download/api/v1/get?type=http";
    }

    @Override
    public List<Proxy> doCollect() {
        String[] urls = new String[]{
                "https://www.proxy-list.download/api/v1/get?type=http",
                "https://www.proxy-list.download/api/v1/get?type=https",
                "https://www.proxy-list.download/api/v1/get?type=socks4",
                "https://www.proxy-list.download/api/v1/get?type=socks5",
        };
        List<Proxy> ret = Lists.newArrayList();
        for (String url : urls) {
            ret.addAll(processLink(url));
        }
        return ret;
    }

    private List<Proxy> processLink(String url) {
        List<Proxy> ret = Lists.newArrayList();
        for (int i = 0; i < 3; i++) {
            try {
                String s = HttpInvoker.get(url);
                if (StringUtils.isEmpty(s)) {
                    continue;
                }
                String[] ips = s.split(IOUtils.LINE_SEPARATOR);
                for (String addr : ips) {
                    Matcher matcher = ipAndPortPattern.matcher(addr);
                    while (matcher.find()) {
                        String ip = matcher.group(1);
                        Integer port = NumberUtils.toInt(matcher.group(4));
                        Proxy proxy = new Proxy();
                        proxy.setIp(ip);
                        proxy.setPort(port);
                        proxy.setConnectionScore(0L);
                        proxy.setAvailbelScore(0L);
                        proxy.setSource(url);
                        ret.add(proxy);
                    }
                }
                return ret;
            } catch (Exception e) {
                logger.error("解析有代理代理详情页异常", e);
            }
        }

        return ret;
    }


    public static void main(String[] args) {
        List<Proxy> proxies = new ProxyListCollector().doCollect();
        System.out.println(JSONObject.toJSONString(proxies));
    }
}
