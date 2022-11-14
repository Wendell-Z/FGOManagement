package com.fgo.management.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DeleteBoostingDetailDto {


    @NotNull(message = "订单编号不能为空!")
    private Long orderId;

    @NotBlank(message = "代练业务类型不能为空")
    private String businessType;
}
