package com.fgo.management.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fgo.management.annotations.OrderDetailToJson;
import com.fgo.management.common.Constants;
import com.fgo.management.dto.OrderBoostingInfo;
import com.fgo.management.dto.OrderStatusInfo;
import com.fgo.management.dto.QueryOrderCondition;
import com.fgo.management.enums.OrderStatus;
import com.fgo.management.mapper.OrderDetailMapper;
import com.fgo.management.model.*;
import com.fgo.management.model.progress.ProgressOverview;
import com.fgo.management.utils.BeanUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderDetailService {

    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ParamConfigService paramConfigService;
    @Autowired
    private BoostingDetailService boostingDetailService;


    @OrderDetailToJson
    @Transactional
    public void insert(OrderDetail orderDetail) {
        orderDetail.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
        orderDetail.setStatus(OrderStatus.INIT);
        BeanUtils.trimStringField(orderDetail);
        BeanUtils.setNullField(orderDetail, Constants.ONE_SPACE_STRING);
        orderDetailMapper.insert(orderDetail);
    }

    @OrderDetailToJson
    @Transactional
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
    @OrderDetailToJson
    public void updateOrderStatus(OrderStatusInfo orderStatusInfo) {
        if (OrderStatus.RUNNING == orderStatusInfo.getOrderStatus()) {
            // 如果是启动的逻辑 要重新梳理 至少判断出启动后 查看去启动哪个业务 设置业务内容
            startBoosting(orderStatusInfo);
        } else {
            orderDetailMapper.updateOrderStatus(orderStatusInfo);
        }
    }

    private void startBoosting(OrderStatusInfo orderStatusInfo) {
        long orderId = orderStatusInfo.getOrderId();
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
            String playerAccount = runningOrders.get(0).getPlayerAccount();
            throw new RuntimeException(String.format("订单ID:%s,是同账号%s玩家，已经在代练中，请先关闭后再开启当前订单的代练！", id, playerAccount));
        } else {
            ParamConfig paramConfig = paramConfigService.queryByParam("BUSINESS", "ORDER");
            List<BusinessOrder> businessOrders = JSONUtil.toList(paramConfig.getParamValue(), BusinessOrder.class);
            businessOrders = businessOrders
                    .stream()
                    .sorted(Comparator.comparing(BusinessOrder::getOrder))
                    .collect(Collectors.toList());
            List<BoostingDetail> boostingDetails = boostingDetailService.queryByOrderId(orderStatusInfo.getOrderId());
            BoostingDetail target = null;
            for (BusinessOrder businessOrder : businessOrders) {
                Optional<BoostingDetail> any = boostingDetails
                        .stream()
                        .filter(item -> item.getBusinessType().equals(businessOrder.getBusinessType()))
                        .findAny();
                if (any.isPresent()) {
                    target = any.get();
                    break;
                }
            }
            if (target == null) {
                throw new RuntimeException("未找到对应的业务类型顺序!");
            }
            orderDetailMapper.setOrderBoostingTask(new OrderBoostingInfo(target.getOrderId(), target.getBoostingTask()));
            orderDetailMapper.updateOrderStatus(orderStatusInfo);
        }
    }

    @OrderDetailToJson
    @Transactional
    public void setOrderBoostingTask(OrderBoostingInfo orderBoostingInfo) {
        orderDetailMapper.setOrderBoostingTask(orderBoostingInfo);
    }

    public OrderDetail queryOrderDetailById(long id) {
        return orderDetailMapper.queryByOrderId(id);
    }

    public void updateOrderSituationById(Long id, String beanJson) {
        orderDetailMapper.updateOrderSituationById(id, beanJson);
    }


    public UserBasicInfo queryBasicInfo(long orderId) {
        String boostingProgress = orderDetailMapper.queryBoostingProgressByOrderId(orderId);
        return StrUtil.isBlank(boostingProgress) ? new UserBasicInfo() : JSONUtil.toBean(boostingProgress, UserBasicInfo.class);
    }

    public ProgressOverview progress(long orderId) {
        String boostingProgress = orderDetailMapper.queryBoostingProgressByOrderId(orderId);
        boostingProgress = "{\n" +
                "    \"orderId\": 123, \n" +
                "    \"refreshTime\": 132, \n" +
                "    \"nickName\": 2134, \n" +
                "    \"gameLevel\": 12, \n" +
                "    \"uidOfFriend\": \"\", \n" +
                "    \"rockAtStart\": 0, \n" +
                "    \"fruitAtStart\": 0, \n" +
                "    \"rockAtCurrent\": 0, \n" +
                "    \"fruitAtCurrent\": 0, \n" +
                "    \"battleCount\": 0, \n" +
                "    \"recentlyLoginTime\": \"123\", \n" +
                "    \"exceptionMessage\": \"123\", \n" +
                "    \"signIn\": { \n" +
                "        \"days\": 123, \n" +
                "        \"daysOfSignedIn\": 0, \n" +
                "        \"lastSignInTime\": 123, \n" +
                "        \"status\": \"\" \n" +
                "    },\n" +
                "    \"boostingLevels\": [ \n" +
                "        {\n" +
                "            \"type\": \"BoostingLevels\", \n" +
                "            \"progress\": \"0/688\", \n" +
                "            \"status\": \"\" \n" +
                "        }\n" +
                "    ],\n" +
                "    \"gatherBalls\": { \n" +
                "        \"ballsCount\": 13, \n" +
                "        \"completedBalls\": 0, \n" +
                "        \"status\": \"\"\n" +
                "    },\n" +
                "    \"gatherDogFood\": { \n" +
                "        \"total\": 1, \n" +
                "        \"doneCount\": 2, \n" +
                "        \"fightCount\": 2, \n" +
                "        \"activePowerCost\": 0, \n" +
                "        \"status\": \"\"\n" +
                "    },\n" +
                "    \"gatherMaterials\": [ \n" +
                "        {\n" +
                "            \"materialName\": \"剑之辉石\", \n" +
                "            \"total\": 1, \n" +
                "            \"doneCount\": 1,\n" +
                "            \"activePowerCost\": 0, \n" +
                "            \"status\": \"\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"gatherGreenSquare\": { \n" +
                "        \"total\": 1, \n" +
                "        \"doneCount:\": 1,\n" +
                "        \"fightCount\": 1,\n" +
                "        \"activePowerCost\": 0,\n" +
                "        \"status\": \"\"\n" +
                "    },\n" +
                "    \"boostingEvents\": [ \n" +
                "        {\n" +
                "            \"eventName\": \"复刻 超古代新遇xxxx\", \n" +
                "            \"boostingContent\": \"活动毕业\", \n" +
                "            \"progress\": \"0/0\", \n" +
                "            \"status\": \"\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"daily\": { \n" +
                "        \"days\": 1, \n" +
                "        \"alsoEvents\": true,\n" +
                "        \"doneDays\": 1, \n" +
                "        \"giftPoolCount\": 1, \n" +
                "        \"donePoolCount\": 1 \n" +
                "    },\n" +
                "    \"FollowerFetters\": [\n" +
                "        {\n" +
                "            \"profession\": \"从者\", \n" +
                "            \"followerName\": \"从者\", \n" +
                "            \"starLevel\": 1,\n" +
                "            \"fettersLevel\": 1, \n" +
                "            \"finalFettersLevel\": 1,\n" +
                "            \"status\": \"\" \n" +
                "        }\n" +
                "    ],\n" +
                "    \"Follower\": [\n" +
                "        {\n" +
                "            \"profession\": \"从者\",\n" +
                "            \"followerName\": \"从者\",\n" +
                "            \"starLevel\": 1,\n" +
                "            \"comeAgainLevel\": 1, \n" +
                "            \"followerLevel\": 1, \n" +
                "            \"skill1Level\": 1, \n" +
                "            \"skill2Level\": 2,\n" +
                "            \"skill3Level\": 3,\n" +
                "            \"levelBreak\": true, \n" +
                "            \"SkillBreak\": true, \n" +
                "            \"holyGrailChange\": true, \n" +
                "            \"holyGrailTotal\": 111 \n" +
                "        }\n" +
                "    ]\n" +
                "}";
        return StrUtil.isBlank(boostingProgress) ? new ProgressOverview() : JSONUtil.toBean(boostingProgress, ProgressOverview.class);
    }

    public void updateOrderStatus(List<String> orderIds, OrderStatus orderStatus) {
        if (!orderIds.isEmpty()) {
            orderDetailMapper.batchUpdateOrderStatus(orderIds, orderStatus);
        }
    }
}
