package com.fgo.management.model.progress;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class PurchaseLevels extends BoostingProgress {

    private String levelName;

    private String boostingContent;

}
