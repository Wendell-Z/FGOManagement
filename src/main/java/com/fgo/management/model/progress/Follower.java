package com.fgo.management.model.progress;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class Follower extends BoostingProgress {

    @EqualsAndHashCode.Exclude
    private String profession;
    @EqualsAndHashCode.Include
    private String followerName;
    @EqualsAndHashCode.Exclude
    private int starLevel;
    @EqualsAndHashCode.Exclude
    private int comeAgainLevel;
    @EqualsAndHashCode.Exclude
    private int followerLevel;
    @EqualsAndHashCode.Exclude
    private int skill1Level;
    @EqualsAndHashCode.Exclude
    private int skill2Level;
    @EqualsAndHashCode.Exclude
    private int skill3Level;
    @EqualsAndHashCode.Exclude
    private boolean levelBreak;
    @EqualsAndHashCode.Exclude
    private boolean skillBreak;
    @EqualsAndHashCode.Exclude
    private boolean holyGrailChange;
    @EqualsAndHashCode.Exclude
    private int holyGrailTotal;


}