package com.mantou.proxyservice.proxeservice.collector;

import com.alibaba.fastjson.JSONObject;
import com.virjar.dungproxy.client.httpclient.HttpInvoker;
import com.virjar.dungproxy.server.crawler.TemplateBuilder;
import com.virjar.dungproxy.server.crawler.impl.TemplateCollector;
import com.virjar.dungproxy.server.entity.Proxy;
import com.virjar.dungproxy.server.utils.Constant;
import org.apache.http.Header;
import org.junit.Test;

import java.util.List;

/**
 * Created by virjar on 16/11/27.
 */
public class JiangXianLiTest {

    @Test
    public void doCollect(){
        String source = "/handmapper_jiangxianli.xml";
        // String source = "/handmapper_proxylistplus.xml";
        TemplateCollector templateCollector = TemplateBuilder.buildfromSource(source).get(0);
        List<Proxy> proxies = templateCollector.doCollect();
        System.out.println(proxies.size());
    }
}
