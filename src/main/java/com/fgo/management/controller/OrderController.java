package com.fgo.management.controller;

import com.fgo.management.annotations.LoginValid;
import com.fgo.management.dto.MyResponse;
import com.fgo.management.dto.OrderStatusInfo;
import com.fgo.management.dto.QueryOrderCondition;
import com.fgo.management.enums.OrderStatus;
import com.fgo.management.model.OrderDetail;
import com.fgo.management.service.OrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    @PostMapping("/search")
    @LoginValid
    public MyResponse queryOrder(HttpServletRequest request, @Validated @RequestBody QueryOrderCondition queryOrderCondition) {
        return MyResponse.success(orderDetailService.pageQueryOrder(queryOrderCondition));
    }

    @PutMapping("/status")
    @LoginValid
    public MyResponse updateOrderStatus(HttpServletRequest request, @Validated @RequestBody OrderStatusInfo orderStatusInfo) {
        return MyResponse.success(orderDetailService.updateOrderStatus(orderStatusInfo));
    }

    @PostMapping("/settled")
    @LoginValid
    public MyResponse settled(HttpServletRequest request, @RequestBody List<String> orderIds) {
        orderDetailService.updateOrderStatus(orderIds, OrderStatus.SETTLED);
        return MyResponse.success();
    }
}
