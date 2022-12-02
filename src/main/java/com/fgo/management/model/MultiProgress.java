package com.fgo.management.model;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class MultiProgress {

    private long orderId;

    private String businessType;

    private String businessKey;

    private String progress;

    private String target;

    private Timestamp createTime;

    private Timestamp lastUpdateTime;
    private String status;
    private Timestamp modifyTime;
}
