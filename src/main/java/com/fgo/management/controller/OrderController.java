package com.fgo.management.controller;

import com.fgo.management.annotations.LoginValid;
import com.fgo.management.dto.MyResponse;
import com.fgo.management.dto.OrderStatusInfo;
import com.fgo.management.dto.QueryOrderCondition;
import com.fgo.management.model.OrderDetail;
import com.fgo.management.service.OrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/order")
@RestController
public class OrderController {


    @Autowired
    private OrderDetailService orderDetailService;

    @PostMapping
    @LoginValid
    public MyResponse newOrder(HttpServletRequest request, @Validated @RequestBody OrderDetail orderDetail) {
        orderDetailService.insert(orderDetail);
        return MyResponse.success();
    }

    @PutMapping
    @LoginValid
    public MyResponse updateOrder(HttpServletRequest request, @Validated @RequestBody OrderDetail orderDetail) {
        orderDetailService.update(orderDetail);
        return MyResponse.success();
    }

    @GetMapping
    @LoginValid
    public MyResponse queryOrder(HttpServletRequest request, @Validated @RequestBody QueryOrderCondition queryOrderCondition) {
        return MyResponse.success(orderDetailService.pageQueryOrder(queryOrderCondition));
    }

    @PutMapping("/status")
    @LoginValid
    public MyResponse updateOrderStatus(HttpServletRequest request, @Validated @RequestBody OrderStatusInfo orderStatusInfo) {
        orderDetailService.updateOrderStatus(orderStatusInfo);
        return MyResponse.success();
    }
}
