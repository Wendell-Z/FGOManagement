package com.fgo.management.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
public class BoostingDetail {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotBlank(message = "业务类型不能为空！")
    private String businessType;

    private String status;

    @NotBlank(message = "代练内容不能为空！")
    private String boostingTask;

    @NotBlank(message = "进度不能为空！")
    private String progress;

    private Timestamp createTime;

    private Timestamp lastUpdateTime;
}
