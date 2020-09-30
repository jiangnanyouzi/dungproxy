package com.virjar.dungproxy.server.crawler.impl;

import com.virjar.dungproxy.server.crawler.TemplateBuilder;
import com.virjar.dungproxy.server.entity.Proxy;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NntimeCollectorTest {

    @Test
    public void testDoCollect() throws Exception {
        String html = IOUtils.toString(TemplateBuilder.class.getResourceAsStream("/11.html"));
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
            // System.out.println(addr);
            matcher = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)").matcher(addr);
            while (matcher.find()) {
                String ip = matcher.group(1);
                System.out.println(ip);
            }

            matcher = Pattern.compile("document.write\\(\":\"\\+(.*)\\)").matcher(addr);
            while (matcher.find()) {
                String portTxt = matcher.group(1);
                StringBuffer stringBuffer = new StringBuffer();
                for (String s : portTxt.split("\\+")) {
                    stringBuffer.append(map.get(s));
                }
                System.out.println(stringBuffer.toString());
            }
        }

    }
}