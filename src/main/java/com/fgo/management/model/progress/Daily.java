package com.fgo.management.model.progress;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class Daily extends BoostingProgress {

    private int days;
    private boolean alsoEvents;
    private int doneDays;
    private int giftPoolCount;
    private int donePoolCount;


}