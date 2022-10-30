package com.fgo.management.controller;

import com.fgo.management.annotations.LoginValid;
import com.fgo.management.dto.MyResponse;
import com.fgo.management.dto.OrderBoostingInfo;
import com.fgo.management.service.OrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/boosting")
@RestController
public class BoostingController {


    @Autowired
    private OrderDetailService orderDetailService;

    @PutMapping
    @LoginValid
    public MyResponse setOrderBoostingTask(@Validated @RequestBody OrderBoostingInfo orderBoostingInfo) {
        orderDetailService.setOrderBoostingTask(orderBoostingInfo);
        return MyResponse.success();
    }
}
