package com.virjar.dungproxy.server.utils;

import com.virjar.dungproxy.client.httpclient.HttpInvoker;
import com.virjar.dungproxy.client.ippool.config.ProxyConstant;
import com.virjar.dungproxy.server.model.ProxyModel;
import org.apache.http.Header;
import org.junit.Test;

public class ProxyUtilTest {

    @Test
    public void httpCheck() {
        // 165.225.12.82&amp;port=10605
        ProxyModel p = new ProxyModel();
        // p.setIp("120.83.106.163");
        p.setIp("67.71.223.61");
        p.setPort(4145);
        // ProxyConstant.CONNECT_TIMEOUT = 60s000;
        // String keysourceurl = "http://ip.taobao.com/outGetIpInfo?ip=202.21.99.42&accessKey=alibaba-inc";
        // String response = HttpInvoker.get(keysourceurl, new Header[]{Constant.CHECK_HEADER}, p.getIp(), p.getPort());
        String keysourceurl = "http://45.77.165.155:8080/proxyipcenter/checkIp";
        String response = HttpInvoker.get(keysourceurl + "?ip=" + p.getIp() + "&port=" + p.getPort(), new Header[]{Constant.CHECK_HEADER}, p.getIp(), p.getPort());
        System.out.println(response);
    }
}