package com.fgo.management.model.progress;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class Follower extends BoostingProgress {

    private String profession;
    private String followerName;
    private int starLevel;
    private int comeAgainLevel;
    private int followerLevel;
    private int skill1Level;
    private int skill2Level;
    private int skill3Level;
    private boolean levelBreak;
    private boolean skillBreak;
    private boolean holyGrailChange;
    private int holyGrailTotal;


}