package com.virjar.dungproxy.server.crawler.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.virjar.dungproxy.client.httpclient.HttpInvoker;
import com.virjar.dungproxy.server.crawler.NewCollector;
import com.virjar.dungproxy.server.entity.Proxy;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * http://nntime.com/proxy-list-01.htm
 * 要翻墙
 */
@Component
public class NntimeCollector extends NewCollector {
    private static final Logger logger = LoggerFactory.getLogger(NntimeCollector.class);
    private String baseURL = "http://nntime.com/proxy-updated-0#{autoincreament}.htm";

    public NntimeCollector() {
        setDuration(24 * 60);
    }

    @Override
    public String lasUrl() {
        return baseURL;
    }

    @Override
    public List<Proxy> doCollect() {
        List<Proxy> ret = Lists.newArrayList();
        for (int i = 1; i < 6; i++) {
            ret.addAll(processLink(baseURL.replace("#{autoincreament}", String.valueOf(i))));
        }
        return ret;
    }

    private List<Proxy> processLink(String url) {
        List<Proxy> ret = Lists.newArrayList();
        for (int i = 0; i < 3; i++) {
            try {
                //要翻墙
                System.out.println(url);
                String html = HttpInvoker.get(url);
                if (StringUtils.isEmpty(html)) {
                    continue;
                }
                HtmlCleaner htmlCleaner = new HtmlCleaner();
                htmlCleaner.getProperties().setUseCdataForScriptAndStyle(false);
                TagNode tagnode = htmlCleaner.clean(html);

                Object[] tbody = tagnode.evaluateXPath("//*[@id='proxylist']/tbody/tr");
                String regex = "<script type=\"text/javascript\">(.*?)</script>";
                Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(html);
                Map<String, Integer> map = new HashMap<>();
                while (matcher.find()) {
                    String[] base = matcher.group(1).trim().split(";");
                    for (String s : base) {
                        String[] ipAndPort = s.split("=");
                        map.put(ipAndPort[0], NumberUtils.toInt(ipAndPort[1], 80));
                    }
                    break;
                }
                for (Object o : tbody) {
                    TagNode child = (TagNode) o;
                    String addr = child.evaluateXPath("/td[2]/text()")[0].toString();
                    matcher = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)").matcher(addr);
                    String ip = null;
                    while (matcher.find()) {
                        ip = matcher.group(1);
                    }
                    int port = 0;
                    matcher = Pattern.compile("document.write\\(\":\"\\+(.*)\\)").matcher(addr);
                    while (matcher.find()) {
                        String portTxt = matcher.group(1);
                        StringBuilder stringBuilder = new StringBuilder();
                        for (String s : portTxt.split("\\+")) {
                            stringBuilder.append(map.get(s));
                        }
                        port = NumberUtils.toInt(stringBuilder.toString());
                    }
                    if (StringUtils.isEmpty(ip) || port == 0) continue;
                    Proxy proxy = new Proxy();
                    proxy.setIp(ip);
                    proxy.setPort(port);
                    proxy.setConnectionScore(0L);
                    proxy.setAvailbelScore(0L);
                    proxy.setSource(lasUrl());
                    ret.add(proxy);
                }
                return ret;
            } catch (Exception e) {
                logger.error("解析有代理代理详情页异常", e);
            }
        }

        return ret;
    }


    public static void main(String[] args) {
        List<Proxy> proxies = new NntimeCollector().doCollect();
        System.out.println(JSONObject.toJSONString(proxies));
    }
}
