package com.fgo.management.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class BusinessOrder {

    @NotBlank(message = "业务名不能为空！")
    private String businessType;

    @NotNull(message = "顺序不能为空！")
    private Integer order;
}
