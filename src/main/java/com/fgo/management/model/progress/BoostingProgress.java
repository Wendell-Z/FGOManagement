package com.fgo.management.model.progress;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class BoostingProgress {

    private String status;

    private Timestamp createTime;

    private String lastUpdateTime;

}
