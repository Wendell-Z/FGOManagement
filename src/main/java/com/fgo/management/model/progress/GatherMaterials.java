package com.fgo.management.model.progress;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class GatherMaterials extends BoostingProgress {

    private String materialName;
    private int total;
    private int doneCount;
    private int activePowerCost;
    private String status;


}