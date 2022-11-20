package com.fgo.management.model.progress;

import cn.hutool.json.JSONUtil;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class ProgressOverview {

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

    public static void main(String[] args) {
        ProgressOverview progressOverview = new ProgressOverview();
        SignIn signIn = new SignIn();
        signIn.setDays(1);
        signIn.setStatus("N");
        signIn.setDaysOfSignedIn(0);
        signIn.setLastUpdateTime("");
        signIn.setLastSignInTime("");
        signIn.setCreateTime(new Timestamp(0));
        progressOverview.setSignIn(signIn);
        System.out.println(JSONUtil.parse(progressOverview).toString());
        System.out.println(System.currentTimeMillis());
        BoostingEvents boostingEvents = new BoostingEvents();
        boostingEvents.setEventName("");
        boostingEvents.setBoostingContent("");
        boostingEvents.setProgress("");
        boostingEvents.setStatus("N");
        boostingEvents.setLastUpdateTime("");
        System.out.println(JSONUtil.toJsonStr(boostingEvents));
        String s = "{\"eventName\":\"\",\"boostingContent\":\"\",\"progress\":\"\",\"status\":\"N\",\"lastUpdateTime\":\"\"}";
    }
}
