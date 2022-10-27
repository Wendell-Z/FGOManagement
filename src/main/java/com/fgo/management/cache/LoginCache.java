package com.fgo.management.cache;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.RandomUtil;
import com.fgo.management.model.UserToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class LoginCache {
    private final static Logger LOGGER = LoggerFactory.getLogger(LoginCache.class);

    private final static Set<UserToken> USER_TOKEN_SET = new ConcurrentHashSet<>();


    public String login(String account) {
        UserToken token = new UserToken();
        token.setAccount(account);
        token.setToken(RandomUtil.randomString(512));
        token.setLoginTime(System.currentTimeMillis());
        USER_TOKEN_SET.add(token);
        return token.getToken();
    }

    public boolean logout(String account, String token) {
        UserToken userToken = new UserToken(account, token);
        return USER_TOKEN_SET.remove(userToken);
    }

    public void clearTimeoutCache() {
        USER_TOKEN_SET.removeIf(userToken -> {
            boolean removed = System.currentTimeMillis() - userToken.getLoginTime() >= 1000 * 60 * 60 * 2;
            if (removed) {
                LOGGER.info("user token timeout,removed. user token: {}", userToken);
            }
            return removed;
        });
    }
}
