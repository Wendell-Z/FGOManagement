package com.fgo.management.schedule;

import com.fgo.management.cache.LoginCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LoginCacheClearTask {
    public static final Logger LOGGER = LoggerFactory.getLogger(LoginCacheClearTask.class);


    @Autowired
    private LoginCache loginCache;

    @Scheduled(initialDelay = 1000, fixedDelay = 1000 * 60)
    public void clearLoginCache() {
        LOGGER.info("clear timeout token cache...");
        loginCache.clearTimeoutCache();
    }
}
