package com.fgo.management.controller;


import com.fgo.management.cache.LoginCache;
import com.fgo.management.dto.MyResponse;
import com.fgo.management.model.UserAccount;
import com.fgo.management.model.UserBasicInfo;
import com.fgo.management.service.OrderDetailService;
import com.fgo.management.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequestMapping("/user")
@RestController

public class UserController {

    public static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private LoginCache loginCache;

    @PostMapping("/login")
    public MyResponse login(@RequestBody UserAccount account, HttpServletRequest request, HttpServletResponse response) {
        UserAccount userAccount = userService.queryUserByUserAccount(account);
        if (userAccount != null) {
            // 校验成功
            // 生成新的token 去掉旧的token
            LOGGER.info("user: {} login,ip: {}", userAccount.getAccount(), request.getRemoteAddr());
            response.setHeader("user-token", loginCache.login(userAccount.getAccount()));
            return MyResponse.success(userAccount);
        } else {
            return MyResponse.failed("用户名或密码错误！");
        }
    }

    @PostMapping("/logout")
    public MyResponse logout(@RequestBody UserAccount userAccount, HttpServletRequest request) {
        String token = request.getHeader("user-token");
        if (loginCache.logout(userAccount.getAccount(), token)) {
            LOGGER.info("user: {} logout,ip: {}", userAccount.getAccount(), request.getRemoteAddr());
        }
        return MyResponse.success();
    }

    @GetMapping("/basic")
    public MyResponse basicInfo(@RequestParam long orderId) {
        // 通过主键获取基本信息
        UserBasicInfo basicInfo = orderDetailService.queryBasicInfo(orderId);
        return MyResponse.success(basicInfo);
    }
}
