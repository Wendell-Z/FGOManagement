package com.fgo.management.model;

import lombok.Data;

@Data
public class UserAccount {

    private long id;

    private String account;

    private String password;

    private String userName;

    private String userRole;


}
