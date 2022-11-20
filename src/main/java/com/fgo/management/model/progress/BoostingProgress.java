package com.fgo.management.model.progress;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;

@Data
@EqualsAndHashCode
public class BoostingProgress {

    @EqualsAndHashCode.Exclude
    private String status;
    @EqualsAndHashCode.Exclude
    private Timestamp createTime;
    @EqualsAndHashCode.Exclude
    private Timestamp lastUpdateTime;

}
