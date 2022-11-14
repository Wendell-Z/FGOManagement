package com.fgo.management.controller;

import com.fgo.management.annotations.LoginValid;
import com.fgo.management.dto.MyResponse;
import com.fgo.management.model.BusinessOrder;
import com.fgo.management.model.ParamConfig;
import com.fgo.management.service.ParamConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/order")
    @LoginValid
    public MyResponse businessOrder(@Validated @RequestBody List<BusinessOrder> businessOrders) {
        paramConfigService.setBusinessOrder(businessOrders);
        return MyResponse.success();
    }
}
