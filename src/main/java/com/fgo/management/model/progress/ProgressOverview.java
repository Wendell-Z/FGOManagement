package com.fgo.management.model.progress;

import lombok.Data;

import java.util.List;

@Data
public class ProgressOverview {

    private long refreshTime;
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
    private List<PurchaseLevels> purchaseLevels;
}
