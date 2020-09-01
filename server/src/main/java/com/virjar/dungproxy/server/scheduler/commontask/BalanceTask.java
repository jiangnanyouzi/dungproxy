package com.virjar.dungproxy.server.scheduler.commontask;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.virjar.dungproxy.server.entity.Proxy;
import com.virjar.dungproxy.server.repository.ProxyLowQualityRepository;
import com.virjar.dungproxy.server.repository.ProxyRepository;
import com.virjar.dungproxy.server.utils.Constant;
import com.virjar.dungproxy.server.utils.SysConfig;

/**
 * Created by nicholas on 9/19/2016.
 */
@Component
public class BalanceTask extends CommonTask {

    private static final Logger logger = LoggerFactory.getLogger(BalanceTask.class);

    @Autowired
    private ProxyRepository proxyRepository;

    @Autowired
    private ProxyLowQualityRepository proxyLowQualityRepository;

    private static final int batchSize = 1000;


    private static final String DURATION = "common.task.duration.balanceTask";
    private static final String STEP = "common.task.duration.balanceTask.step";
    private static final String THRESHOLD = "common.task.duration.balanceTask.threshold";

    public BalanceTask() {
        super(NumberUtils.toInt(SysConfig.getInstance().get(DURATION), 176400000));
        Constant.STEP = NumberUtils.toInt(SysConfig.getInstance().get(STEP), Constant.STEP);
        Constant.THRESHOLD = NumberUtils.toInt(SysConfig.getInstance().get(THRESHOLD), Constant.THRESHOLD);
    }

    private boolean insertAndDelete(int step, int threshold) {

        int insert = 0;
        int delete = 0;
        while (true) {
            List<Proxy> lowProxy = proxyRepository.getLowProxy(step, threshold, new PageRequest(0, batchSize));
            if (lowProxy.isEmpty()) {
                return insert == delete;
            }
            List<Long> ids = Lists.newArrayList();
            for (Proxy proxy : lowProxy) {
                logger.info("move low proxy resource:{}", JSONObject.toJSONString(proxy));
                insert += proxyLowQualityRepository.insert(proxy);
                ids.add(proxy.getId());
            }
            proxyRepository.deleteBatch(ids);
        }

    }

    @Override
    public Object execute() {
        logger.info("Constant.STEP {} , Constant.THRESHOLD {},begin migrate data....", Constant.STEP, Constant.THRESHOLD);
        boolean result = insertAndDelete(Constant.STEP, Constant.THRESHOLD);
        logger.info("end migrate data result = " + result);
        return result;
    }
}
