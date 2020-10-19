package com.mantou.proxyservice.proxeservice.collector;

import com.alibaba.fastjson.JSONObject;
import com.virjar.dungproxy.client.httpclient.HttpInvoker;
import com.virjar.dungproxy.server.crawler.TemplateBuilder;
import com.virjar.dungproxy.server.crawler.extractor.XmlModeFetcher;
import com.virjar.dungproxy.server.crawler.impl.TemplateCollector;
import com.virjar.dungproxy.server.entity.Proxy;
import com.virjar.dungproxy.server.utils.Constant;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.dom4j.DocumentException;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeTraversor;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created by virjar on 16/11/27.
 */
public class JiangXianLiTest {

    @Test
    public void doCollect() throws Exception {
        String source = "/handmapper_proxynova.xml";
        // String source = "/fetcher/nntime_com.xml";
        // String source = "/handmapper_jiangxianli.xml";
        // String source = "/handmapper_proxylistplus.xml";
        TemplateCollector templateCollector = TemplateBuilder.buildfromSource(source).get(0);
        List<Proxy> proxies = templateCollector.doCollect();
        System.out.println(proxies.size());

        // System.out.println(IOUtils.toString(TemplateBuilder.class.getResourceAsStream(source)));
        // XmlModeFetcher xmlModeFetcher = new XmlModeFetcher(IOUtils.toString(TemplateBuilder.class.getResourceAsStream(source)));
        // List<String> list = xmlModeFetcher.fetch(IOUtils.toString(TemplateBuilder.class.getResourceAsStream("/11.html")));
        // System.out.println(list.size());
        // for (String s : list) {
        //     System.out.println(s);
        // }

    }
}
