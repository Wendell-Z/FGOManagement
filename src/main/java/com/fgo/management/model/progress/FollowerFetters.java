package com.fgo.management.model.progress;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class FollowerFetters extends BoostingProgress {

    private String profession;
    private String followerName;
    private int starLevel;
    private int fettersLevel;
    private int finalFettersLevel;

}