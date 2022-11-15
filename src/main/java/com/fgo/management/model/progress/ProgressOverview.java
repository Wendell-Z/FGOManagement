package com.fgo.management.model.progress;

import lombok.Data;

import java.util.List;

@Data
public class ProgressOverview {

    private int orderId;
    private int refreshTime;
    private int nickName;
    private int gameLevel;
    private String uidOfFriend;
    private int rockAtStart;
    private int fruitAtStart;
    private int rockAtCurrent;
    private int fruitAtCurrent;
    private int battleCount;
    private String recentlyLoginTime;
    private String exceptionMessage;
    private SignIn signIn;
    private List<BoostingLevels> boostingLevels;
    private GatherBalls gatherBalls;
    private GatherDogFood gatherDogFood;
    private List<GatherMaterials> gatherMaterials;
    private GatherGreenSquare gatherGreenSquare;
    private List<BoostingEvents> boostingEvents;
    private Daily daily;
    private List<FollowerFetters> FollowerFetters;
    private List<Follower> Follower;
}
