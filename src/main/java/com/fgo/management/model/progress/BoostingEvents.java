package com.fgo.management.model.progress;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class BoostingEvents extends BoostingProgress {

    private String eventName;
    private String boostingContent;
    private String progress;


}