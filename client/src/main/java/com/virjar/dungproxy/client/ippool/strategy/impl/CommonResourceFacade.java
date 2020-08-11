package com.virjar.dungproxy.client.ippool.strategy.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.virjar.dungproxy.client.httpclient.HttpInvoker;
import com.virjar.dungproxy.client.httpclient.NameValuePairBuilder;
import com.virjar.dungproxy.client.ippool.strategy.ResourceFacade;
import com.virjar.dungproxy.client.model.AvProxyVO;
import com.virjar.dungproxy.client.model.FeedBackForm;
import com.virjar.dungproxy.client.util.PoolUtil;
import com.virjar.dungproxy.client.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.protocol.HttpClientContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Created by virjar on 16/9/29.
 */
public class CommonResourceFacade implements ResourceFacade {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private String downloadSign = null;
    private static String clientID = null;
    private static String avUrl;
    private static String feedBackUrl;
    private static String allAvUrl;
    private static String allAvPageUrl;

    static final String RESOURCE_FACADE_AVURL = "proxyclient.resouce.resourceFacade.avUrl";
    static final String RESOURCE_FACADE_FEEDBACKURL = "proxyclient.resouce.resourceFacade.feedBackUrl";
    static final String RESOURCE_FACADE_ALLAVURL = "proxyclient.resouce.resourceFacade.allAvUrl";
    static final String RESOURCE_FACADE_ALLAVPAGEURL = "proxyclient.resouce.resourceFacade.allAvPageUrl";

    static {
        avUrl = PropertiesUtil.getProperty(RESOURCE_FACADE_AVURL);
        feedBackUrl = PropertiesUtil.getProperty(RESOURCE_FACADE_FEEDBACKURL);
        allAvUrl = PropertiesUtil.getProperty(RESOURCE_FACADE_ALLAVURL);
        allAvPageUrl = PropertiesUtil.getProperty(RESOURCE_FACADE_ALLAVPAGEURL);
    }


    public static void setAvUrl(String avUrl) {
        CommonResourceFacade.avUrl = avUrl;
    }

    public static void setFeedBackUrl(String feedBackUrl) {
        CommonResourceFacade.feedBackUrl = feedBackUrl;
    }

    public static void setAllAvUrl(String allAvUrl) {
        CommonResourceFacade.allAvUrl = allAvUrl;
    }

    public static void setClientID(String clientID) {
        CommonResourceFacade.clientID = clientID;
    }

    public static void setAllAvPageUrl(String allAvPageUrl) {
        CommonResourceFacade.allAvPageUrl = allAvPageUrl;
    }

    @Override
    public List<AvProxyVO> importProxy(String domain, String testUrl, Integer number) {
        if (number == null || number < 1) {
            number = 30;
        }
        NameValuePairBuilder nameValuePairBuilder = NameValuePairBuilder.create();
        if (StringUtils.isNotEmpty(clientID)) {
            nameValuePairBuilder.addParam("clientID", clientID);
        } else {
            nameValuePairBuilder.addParam("usedSign", downloadSign);
        }
        nameValuePairBuilder.addParam("checkUrl", testUrl).addParam("domain", domain).addParam("num",
                String.valueOf(number));
        List<NameValuePair> valuePairList = nameValuePairBuilder.build();

        logger.info("默认IP下载器,IP下载URL:{}", avUrl);
        HttpClientContext httpClientContext = HttpClientContext.create();
        PoolUtil.disableDungProxy(httpClientContext);
        String response = HttpInvoker.post(avUrl, valuePairList, httpClientContext);

        if (StringUtils.isBlank(response)) {
            logger.error("can not get available ip resource from server: request body is {}",
                    JSONObject.toJSONString(valuePairList));
            return Lists.newArrayList();
        }
        JSONObject jsonObject = JSONObject.parseObject(response);
        if (!jsonObject.getBoolean("status")) {
            logger.error("can not get available ip resource from server:request  body is  last response is ",
                    JSONObject.toJSONString(valuePairList), response);
            return Lists.newArrayList();
        }
        jsonObject = jsonObject.getJSONObject("data");
        this.downloadSign = jsonObject.getString("sign");
        return convert(jsonObject.getJSONArray("data"));
    }

    @Override
    public void feedBack(String domain, List<AvProxyVO> avProxies, List<AvProxyVO> disableProxies) {
        Preconditions.checkNotNull(domain);
        Preconditions.checkNotNull(avProxies);
        Preconditions.checkNotNull(disableProxies);
        FeedBackForm feedBackForm = new FeedBackForm();
        feedBackForm.setDomain(domain);
        feedBackForm.setAvProxy(avProxies);
        feedBackForm.setDisableProxy(disableProxies);
        HttpClientContext httpClientContext = HttpClientContext.create();
        PoolUtil.disableDungProxy(httpClientContext);
        HttpInvoker.postJSON(feedBackUrl, feedBackForm, httpClientContext);
    }

    @Override
    public List<AvProxyVO> allAvailable() {
        String fetchIpUrl = StringUtils.isEmpty(allAvPageUrl) ? allAvUrl : allAvPageUrl;
        boolean page = StringUtils.isEmpty(allAvPageUrl);
        logger.info("默认IP下载器,IP下载URL:{} {} ", fetchIpUrl, page);
        List<AvProxyVO> ret = Lists.newArrayList();
        int pageNum = 1;
        while (true) {
            if (page) fetchIpUrl = fetchIpUrl.replace("pageNum", String.valueOf(pageNum));
            HttpClientContext httpClientContext = HttpClientContext.create();
            PoolUtil.disableDungProxy(httpClientContext);
            String response = HttpInvoker.get(fetchIpUrl, httpClientContext);
            if (StringUtils.isBlank(response)) {
                logger.error("can not get available ip resource from server: ");
                break;
            }
            JSONObject jsonObject = JSONObject.parseObject(response);
            if (!jsonObject.getBoolean("status")) {
                logger.error("can not get available ip resource from server:request  body is  last response is {} ", response);
                break;
            }
            //说明没有ip了,采集 分页 结束
            if (!jsonObject.containsKey("data")) {
                logger.error("data is not exists {} ", response);
                break;
            }
            ret.addAll(convert(jsonObject.getJSONArray("data")));
            pageNum++;
            if (!page) break;
        }
        return ret;
    }

    private List<AvProxyVO> convert(JSONArray jsonArray) {
        List<AvProxyVO> ret = Lists.newArrayList();
        for (Object obj : jsonArray) {
            JSONObject proxy = (JSONObject) obj;
            AvProxyVO avProxy = new AvProxyVO();
            avProxy.setIp(proxy.getString("ip"));
            avProxy.setPort(proxy.getInteger("port"));
            ret.add(avProxy);
        }
        return ret;
    }
}
