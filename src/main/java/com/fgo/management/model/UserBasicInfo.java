package com.fgo.management.model;

import lombok.Data;

@Data
public class UserBasicInfo {

    private String nickName;
    private int gameLevel;
    private int activePower;
    private int rockAtCurrent;
    private int fruitAtCurrent;
    private String uidOfFriend;

}
