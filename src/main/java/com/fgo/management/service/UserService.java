package com.fgo.management.service;

import com.fgo.management.mapper.UserMapper;
import com.fgo.management.model.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {


    @Autowired
    private UserMapper userMapper;

    public UserAccount queryUserByUserAccount(UserAccount userAccount) {
        return userMapper.queryUserByUserAccount(userAccount);
    }
}
