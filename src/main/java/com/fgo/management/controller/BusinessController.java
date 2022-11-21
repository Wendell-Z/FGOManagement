package com.fgo.management.controller;

import cn.hutool.json.JSONUtil;
import com.fgo.management.annotations.LoginValid;
import com.fgo.management.dto.GlobalActivePower;
import com.fgo.management.dto.MyResponse;
import com.fgo.management.model.BusinessOrder;
import com.fgo.management.model.ParamConfig;
import com.fgo.management.service.OrderDetailService;
import com.fgo.management.service.ParamConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/business")
@RestController
public class BusinessController {

    @Autowired
    private ParamConfigService paramConfigService;
    @Autowired
    private OrderDetailService orderDetailService;

    @PutMapping("/event/activePower")
    @LoginValid
    public MyResponse setEventActivePower(HttpServletRequest request, @Validated @RequestBody GlobalActivePower globalActivePower) {
        paramConfigService.updateParamValue("EVENT", "ACTIVE_POWER", JSONUtil.toJsonStr(globalActivePower));
        return MyResponse.success();
    }

    @GetMapping("/event/activePower")
    @LoginValid
    public MyResponse get(HttpServletRequest request) {
        GlobalActivePower globalActivePower = JSONUtil.toBean(paramConfigService.queryByParam("EVENT", "ACTIVE_POWER")
                .getParamValue(), GlobalActivePower.class);
        return MyResponse.success(globalActivePower);
    }

    @PostMapping("/event/activePower")
    @LoginValid
    public MyResponse businessOrder(HttpServletRequest request, @Validated @RequestBody List<BusinessOrder> businessOrders) {
        paramConfigService.setBusinessOrder(businessOrders);
        return MyResponse.success();
    }

    @GetMapping("/progressOverview")
    @LoginValid
    public MyResponse progress(HttpServletRequest request, @RequestParam long orderId) {
        // 订单服务
        return MyResponse.success(orderDetailService.progress(orderId));
    }

    @GetMapping("/order")
    @LoginValid
    public MyResponse businessOrder(HttpServletRequest request) {
        ParamConfig paramConfig = paramConfigService.queryByParam("BUSINESS", "ORDER");
        return MyResponse.success(JSONUtil.toList(paramConfig.getParamValue(), BusinessOrder.class)
                .stream()
                .sorted(Comparator.comparing(BusinessOrder::getOrder))
                .collect(Collectors.toList()));
    }
}
