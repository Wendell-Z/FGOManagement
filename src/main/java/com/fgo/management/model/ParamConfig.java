package com.fgo.management.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ParamConfig {

    @NotBlank(message = "子参数不能为空！")
    public String subParam;
    private long id;
    @NotBlank(message = "根参数不能为空！")
    private String rootParam;
    private String paramDesc;

    @NotBlank(message = "参数值不能为空！")
    private String paramValue;
}
