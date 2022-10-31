package com.fgo.management.dto;

import com.fgo.management.enums.OrderStatus;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class OrderStatusInfo {

    @NotNull(message = "订单ID不能为空！")
    @Min(value = 0, message = "订单ID>0!")
    private long orderId;

    @NotNull(message = "订单状态不能为空！")
    private OrderStatus orderStatus;
}
