package com.fgo.management.aspects;

import com.fgo.management.cache.LoginCache;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class LoginValidAspect {

    @Autowired
    private LoginCache loginCache;

    @Pointcut("@annotation(com.fgo.management.annotations.LoginValid)")
    public void pointcut() {

    }

    @Before("@annotation(com.fgo.management.annotations.LoginValid)")
    public void before(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        HttpServletRequest arg = (HttpServletRequest) args[0];
        String token = arg.getHeader("user-token");
        if (!loginCache.isTokenValid(token)) {
            throw new RuntimeException("用户登录信息已失效，请重新登录！");
        }
    }

}
