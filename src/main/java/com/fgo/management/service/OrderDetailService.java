package com.fgo.management.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fgo.management.annotations.OrderDetailToJson;
import com.fgo.management.common.Constants;
import com.fgo.management.dto.OrderBoostingInfo;
import com.fgo.management.dto.OrderStatusInfo;
import com.fgo.management.dto.QueryOrderCondition;
import com.fgo.management.enums.BusinessType;
import com.fgo.management.enums.OrderStatus;
import com.fgo.management.mapper.OrderDetailMapper;
import com.fgo.management.model.*;
import com.fgo.management.model.progress.*;
import com.fgo.management.utils.BeanUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
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
    public String updateOrderStatus(OrderStatusInfo orderStatusInfo) {
        if (OrderStatus.RUNNING == orderStatusInfo.getOrderStatus()) {
            // 如果是启动的逻辑 要重新梳理 至少判断出启动后 查看去启动哪个业务 设置业务内容
            return startBoosting(orderStatusInfo);
        } else {
            orderDetailMapper.updateOrderStatus(orderStatusInfo);
        }
        return Constants.EMPTY_STRING;
    }

    private String startBoosting(OrderStatusInfo orderStatusInfo) {
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
            return String.format("订单ID:%s,是同账号%s玩家，已经在代练中，请先关闭后再开启当前订单的代练！", id, playerAccount);
        } else {
            ParamConfig paramConfig = paramConfigService.queryByParam("BUSINESS", "ORDER");
            List<BusinessOrder> businessOrders = JSONUtil.toList(paramConfig.getParamValue(), BusinessOrder.class);
            businessOrders = businessOrders
                    .stream()
                    .sorted(Comparator.comparing(BusinessOrder::getOrder))
                    .collect(Collectors.toList());
            List<BoostingDetail> boostingDetails = boostingDetailService.queryByOrderId(orderStatusInfo.getOrderId());
            BoostingDetail target = null;
            List<BoostingDetail> executeList = new ArrayList<>();
            for (BusinessOrder businessOrder : businessOrders) {
                Optional<BoostingDetail> any = boostingDetails
                        .stream()
                        .filter(item -> item.getBusinessType().equals(businessOrder.getBusinessType()))
                        .findAny();
                if (any.isPresent()) {
                    target = any.get();
                    executeList.add(target);
                }
            }
            if (!executeList.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                executeList.forEach(item -> sb.append(item.getBoostingTask()).append(";"));
                orderDetailMapper.setOrderBoostingTask(new OrderBoostingInfo(target.getOrderId(), sb.toString()));
                orderDetailMapper.updateOrderStatus(orderStatusInfo);
            } else {
                throw new RuntimeException("未设置代练业务！");
            }
        }
        return Constants.EMPTY_STRING;
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
        // 查询这个订单的所有的业务记录
        // 把进度拿进来json转换成类 返回
        List<BoostingDetail> boostingDetails = boostingDetailService.queryProgressByOrderId(orderId);
        Map<BusinessType, BoostingDetail> boostingDetailMap = boostingDetails
                .stream()
                .collect(Collectors.toMap(item -> BusinessType.valueOf(item.getBusinessType()), item -> item));
        ProgressOverview progressOverview = new ProgressOverview();
        if (boostingDetailMap.containsKey(BusinessType.Daily)) {
            progressOverview.setDaily(JSONUtil.toBean(boostingDetailMap.get(BusinessType.Daily).getProgress(), Daily.class));
        }
        if (boostingDetailMap.containsKey(BusinessType.GatherBalls)) {
            progressOverview.setGatherBalls(JSONUtil.toBean(boostingDetailMap.get(BusinessType.GatherBalls).getProgress(), GatherBalls.class));
        }
        if (boostingDetailMap.containsKey(BusinessType.GatherDogFood)) {
            progressOverview.setGatherDogFood(JSONUtil.toBean(boostingDetailMap.get(BusinessType.GatherDogFood).getProgress(), GatherDogFood.class));
        }
        if (boostingDetailMap.containsKey(BusinessType.GatherGreenSquare)) {
            progressOverview.setGatherGreenSquare(JSONUtil.toBean(boostingDetailMap.get(BusinessType.GatherGreenSquare).getProgress(), GatherGreenSquare.class));
        }
        if (boostingDetailMap.containsKey(BusinessType.SignIn)) {
            progressOverview.setSignIn(JSONUtil.toBean(boostingDetailMap.get(BusinessType.SignIn).getProgress(), SignIn.class));
        }
        if (boostingDetailMap.containsKey(BusinessType.Follower)) {
            progressOverview.setFollower(JSONUtil.toList(boostingDetailMap.get(BusinessType.Follower).getProgress(), Follower.class));
        }
        if (boostingDetailMap.containsKey(BusinessType.FollowerFetters)) {
            progressOverview.setFollowerFetters(JSONUtil.toList(boostingDetailMap.get(BusinessType.FollowerFetters).getProgress(), FollowerFetters.class));
        }
        if (boostingDetailMap.containsKey(BusinessType.GatherMaterials)) {
            progressOverview.setGatherMaterials(JSONUtil.toList(boostingDetailMap.get(BusinessType.GatherMaterials).getProgress(), GatherMaterials.class));
        }
        if (boostingDetailMap.containsKey(BusinessType.BoostingEvents)) {
            progressOverview.setBoostingEvents(JSONUtil.toList(boostingDetailMap.get(BusinessType.BoostingEvents).getProgress(), BoostingEvents.class));
        }
        if (boostingDetailMap.containsKey(BusinessType.PurchaseLevels)) {
            progressOverview.setPurchaseLevels(JSONUtil.toList(boostingDetailMap.get(BusinessType.PurchaseLevels).getProgress(), PurchaseLevels.class));
        }
        if (boostingDetailMap.containsKey(BusinessType.BoostingLevels)) {
            progressOverview.setBoostingLevels(JSONUtil.toList(boostingDetailMap.get(BusinessType.BoostingLevels).getProgress(), BoostingLevels.class));
        }
        if (boostingDetailMap.containsKey(BusinessType.GatherQP)) {
            progressOverview.setGatherQP(JSONUtil.toBean(boostingDetailMap.get(BusinessType.GatherQP).getProgress(), GatherQP.class));
        }
        return progressOverview;
    }

    public void updateOrderStatus(List<String> orderIds, OrderStatus orderStatus) {
        if (!orderIds.isEmpty()) {
            orderDetailMapper.batchUpdateOrderStatus(orderIds, orderStatus);
        }
    }

    public List<OrderDetail> queryByUpdateStatus(String updateStatus) {
        return orderDetailMapper.queryByUpdateStatus(updateStatus);
    }

    public void updateProgress(OrderDetail orderDetail) {
        orderDetailMapper.updateProgress(orderDetail);
    }
}

