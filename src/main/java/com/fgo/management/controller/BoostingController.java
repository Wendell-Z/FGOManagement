package com.fgo.management.controller;

import com.fgo.management.annotations.LoginValid;
import com.fgo.management.dto.DeleteBoostingDetailDto;
import com.fgo.management.dto.MyResponse;
import com.fgo.management.model.BoostingDetail;
import com.fgo.management.service.BoostingDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/boosting")
@RestController
public class BoostingController {


    @Autowired
    private BoostingDetailService boostingDetailService;

    @PostMapping
    @LoginValid
    public MyResponse mergeBusinessDetail(@Validated @RequestBody BoostingDetail boostingDetail) {
        boostingDetailService.merge(boostingDetail);
        return MyResponse.success();
    }

    @DeleteMapping
    @LoginValid
    public MyResponse deleteBusinessDetail(@Validated @RequestBody DeleteBoostingDetailDto deleteBoostingDetailDto) {
        boostingDetailService.delete(deleteBoostingDetailDto.getOrderId(), deleteBoostingDetailDto.getBusinessType());
        return MyResponse.success();
    }


}
