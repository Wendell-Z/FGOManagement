package com.fgo.management.model.progress;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class GatherDogFood extends BoostingProgress {

    private int total;
    private int doneCount;
    private int fightCount;
    private int activePowerCost;
    private String status;

}