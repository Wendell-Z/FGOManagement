package com.fgo.management.service;

import com.fgo.management.common.Constants;
import com.fgo.management.dto.OrderStatusInfo;
import com.fgo.management.dto.QueryOrderCondition;
import com.fgo.management.enums.OrderStatus;
import com.fgo.management.mapper.OrderDetailMapper;
import com.fgo.management.model.OrderDetail;
import com.fgo.management.utils.BeanUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderDetailService {

    @Autowired
    private OrderDetailMapper orderDetailMapper;


    public void insert(OrderDetail orderDetail) {
        orderDetail.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
        orderDetail.setStatus(OrderStatus.INIT);
        BeanUtils.trimStringField(orderDetail);
        BeanUtils.setNullField(orderDetail, Constants.ONE_SPACE_STRING);
        orderDetailMapper.insert(orderDetail);
    }

    public void update(OrderDetail orderDetail) {
        BeanUtils.trimStringField(orderDetail);
        BeanUtils.setNullField(orderDetail, Constants.ONE_SPACE_STRING);
        orderDetailMapper.update(orderDetail);
    }

    public PageInfo<OrderDetail> pageQueryOrder(QueryOrderCondition condition) {
        PageHelper.startPage(condition.getPageNum(), condition.getPageSize(), true);
        List<OrderDetail> orderDetails = orderDetailMapper.queryOrderDetails(condition);
        return new PageInfo<>(orderDetails);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(OrderStatusInfo orderStatusInfo) {
        int orderId = orderStatusInfo.getOrderId();
        OrderDetail orderDetail = orderDetailMapper.queryByOrderId(orderId);
        // LOCK EVERY order with same account
        List<OrderDetail> orderDetailList = orderDetailMapper.queryByPlayerAccountWithLock(orderDetail.getPlayerAccount());
        List<OrderDetail> runningOrders = orderDetailList
                .stream()
                .filter(item -> item.getStatus() == OrderStatus.RUNNING)
                .filter(item -> item.getId() != orderId)
                .collect(Collectors.toList());
        if (!runningOrders.isEmpty()) {
            long id = runningOrders.get(0).getId();
            throw new RuntimeException(String.format("订单ID:%s是同账号玩家，已经在代练中，请先关闭后再开启当前订单的代练！", id));
        } else {
            orderDetailMapper.updateOrderStatus(orderStatusInfo);
        }
    }
}
