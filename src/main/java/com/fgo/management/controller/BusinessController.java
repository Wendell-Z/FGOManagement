package com.fgo.management.controller;

import com.fgo.management.annotations.LoginValid;
import com.fgo.management.dto.MyResponse;
import com.fgo.management.model.ParamConfig;
import com.fgo.management.service.ParamConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/business")
@RestController
public class BusinessController {


    @Autowired
    private ParamConfigService paramConfigService;

    @PutMapping("/event/activePower")
    @LoginValid
    public MyResponse setEventActivePower(@Validated @RequestBody ParamConfig paramConfig) {
        paramConfigService.updateParamValue(paramConfig.getRootParam(), paramConfig.getSubParam(), paramConfig.getParamValue());
        return MyResponse.success();
    }
}
