package com.fgo.management.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateProgressTask {

    public static final Logger LOGGER = LoggerFactory.getLogger(LoginCacheClearTask.class);


    @Autowired

    //@Scheduled(initialDelay = 1000, fixedDelay = 1000)
    public void updateProgressTask() {
        LOGGER.info("update progress");
        // 查出来进度数据
        // 查出来业务名
        // 根据业务名取进度数据的值 取的到就设置
    }
}
