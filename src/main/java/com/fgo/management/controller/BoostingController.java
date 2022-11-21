package com.fgo.management.controller;

import com.fgo.management.annotations.LoginValid;
import com.fgo.management.dto.MyResponse;
import com.fgo.management.model.BoostingDetail;
import com.fgo.management.service.BoostingDetailService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/boosting")
@RestController
public class BoostingController {


    @Autowired
    private BoostingDetailService boostingDetailService;

    @PostMapping
    @LoginValid
    public MyResponse mergeBusinessDetail(HttpServletRequest request, @Validated @RequestBody BoostingDetail boostingDetail) {
        boostingDetailService.merge(boostingDetail);
        return MyResponse.success();
    }

    @DeleteMapping
    @LoginValid
    public MyResponse deleteBusinessDetail(HttpServletRequest request, @RequestParam("orderId") long orderId, @Param("businessType") String businessType) {
        boostingDetailService.delete(orderId, businessType);
        return MyResponse.success();
    }

    @GetMapping("/info/follower")
    public MyResponse getFollowerInfo(HttpServletRequest request, @RequestParam("orderId") long orderId) {
        return MyResponse.success(boostingDetailService.getFollowerInfo(orderId));
    }


}
