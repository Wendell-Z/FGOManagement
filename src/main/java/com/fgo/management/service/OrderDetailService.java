package com.fgo.management.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fgo.management.annotations.OrderDetailToJson;
import com.fgo.management.common.Constants;
import com.fgo.management.dto.OrderBoostingInfo;
import com.fgo.management.dto.OrderStatusInfo;
import com.fgo.management.dto.QueryOrderCondition;
import com.fgo.management.enums.BoostingStatus;
import com.fgo.management.enums.BusinessType;
import com.fgo.management.enums.OrderStatus;
import com.fgo.management.mapper.OrderDetailMapper;
import com.fgo.management.model.*;
import com.fgo.management.model.progress.*;
import com.fgo.management.utils.BeanUtils;
import com.fgo.management.utils.TimeUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    @Autowired
    private MultiProgressService multiProgressService;


    @OrderDetailToJson
    @Transactional
    public void insert(OrderDetail orderDetail) {
        orderDetail.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
        orderDetail.setStatus(OrderStatus.INIT);
        orderDetail.setOrderAmount(BigDecimal.ZERO);
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
        orderDetails
                .stream()
                .filter(item -> OrderStatus.SETTLED == item.getOrderStatus())
                .forEach(item -> item.setStatus(OrderStatus.SETTLED));
        orderDetails.forEach(item -> item.setCreateTime(TimeUtils.timestampPlus8(item.getCreateTime())));
        return new PageInfo<>(orderDetails);
    }

    @Transactional(rollbackFor = Exception.class)
    @OrderDetailToJson
    public String updateOrderStatus(OrderStatusInfo orderStatusInfo) {
        if (OrderStatus.RUNNING == orderStatusInfo.getOrderStatus()) {
            // 这里实际上只改开关了
            return startBoosting(orderStatusInfo);
        } else {
            // 这里可能既要改开关又要改状态
            orderDetailMapper.updateOrderStatus(orderStatusInfo);
        }
        return Constants.EMPTY_STRING;
    }

    private String startBoosting(OrderStatusInfo orderStatusInfo) {
        String message = Constants.EMPTY_STRING;
        long orderId = orderStatusInfo.getOrderId();
        OrderDetail orderDetail = orderDetailMapper.queryByOrderId(orderId);
        if (OrderStatus.RUNNING == orderDetail.getStatus()) {
            throw new RuntimeException("该订单已经在运行中！");
        }
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
            message = String.format("订单ID:%s,是同账号%s玩家，已经在代练中，当前订单不会立即开始代练！", id, playerAccount);
        }
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
        if (target != null) {
            if (BoostingStatus.N.name().equals(target.getStatus())) {
                orderDetailMapper.setOrderBoostingTask(new OrderBoostingInfo(target.getOrderId(), target.getBoostingTask()));
                orderDetailMapper.updateOrderStatus(orderStatusInfo);
            } else {
                throw new RuntimeException("该订单已完成或处在异常状态，请处理后再启动！");
            }
        } else {
            throw new RuntimeException("该订单还未设置代练业务！");
        }
        return message;
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
        List<MultiProgress> multiProgressList = multiProgressService.queryProgressByOrderId(orderId);
        Map<BusinessType, BoostingDetail> boostingDetailMap = boostingDetails
                .stream()
                .collect(Collectors.toMap(item -> BusinessType.valueOf(item.getBusinessType()), item -> item));
        Map<BusinessType, List<MultiProgress>> multiProgressMap = multiProgressList.stream()
                .collect(Collectors.toMap(item -> BusinessType.valueOf(item.getBusinessType()), item -> {
                    List<MultiProgress> multiProgresses = new ArrayList<>();
                    multiProgresses.add(item);
                    return multiProgresses;
                }, (List<MultiProgress> value1, List<MultiProgress> value2) -> {
                    value1.addAll(value2);
                    return value1;
                }));
        ProgressOverview progressOverview = new ProgressOverview();
        if (boostingDetailMap.containsKey(BusinessType.Daily)) {
            BoostingDetail detail = boostingDetailMap.get(BusinessType.Daily);
            String target = detail.getTarget();
            Daily daily = JSONUtil.toBean(detail.getProgress(), Daily.class);
            // refresh target
            daily.setDays(Integer.parseInt(target));
            daily.setCreateTime(TimeUtils.timestampPlus8(detail.getCreateTime()));
            daily.setLastUpdateTime(TimeUtils.timestampPlus8(detail.getLastUpdateTime()));
            progressOverview.setDaily(daily);
        }
        if (boostingDetailMap.containsKey(BusinessType.GatherBalls)) {
            BoostingDetail detail = boostingDetailMap.get(BusinessType.GatherBalls);
            String target = detail.getTarget();
            GatherBalls gatherBalls = JSONUtil.toBean(detail.getProgress(), GatherBalls.class);
            // refresh target
            gatherBalls.setBallsCount(Integer.parseInt(target));
            gatherBalls.setCreateTime(TimeUtils.timestampPlus8(detail.getCreateTime()));
            gatherBalls.setLastUpdateTime(TimeUtils.timestampPlus8(detail.getLastUpdateTime()));
            progressOverview.setGatherBalls(gatherBalls);
        }
        if (boostingDetailMap.containsKey(BusinessType.GatherDogFood)) {
            BoostingDetail detail = boostingDetailMap.get(BusinessType.GatherDogFood);
            String target = detail.getTarget();
            GatherDogFood gatherDogFood = JSONUtil.toBean(detail.getProgress(), GatherDogFood.class);
            // refresh target
            gatherDogFood.setTotal(Integer.parseInt(target));
            gatherDogFood.setCreateTime(TimeUtils.timestampPlus8(detail.getCreateTime()));
            gatherDogFood.setLastUpdateTime(TimeUtils.timestampPlus8(detail.getLastUpdateTime()));
            progressOverview.setGatherDogFood(gatherDogFood);
        }
        if (boostingDetailMap.containsKey(BusinessType.GatherGreenSquare)) {
            BoostingDetail detail = boostingDetailMap.get(BusinessType.GatherGreenSquare);
            String target = detail.getTarget();
            GatherGreenSquare gatherGreenSquare = JSONUtil.toBean(detail.getProgress(), GatherGreenSquare.class);
            // refresh target
            gatherGreenSquare.setTotal(Integer.parseInt(target));
            gatherGreenSquare.setCreateTime(TimeUtils.timestampPlus8(detail.getCreateTime()));
            gatherGreenSquare.setLastUpdateTime(TimeUtils.timestampPlus8(detail.getLastUpdateTime()));
            progressOverview.setGatherGreenSquare(gatherGreenSquare);
        }
        if (boostingDetailMap.containsKey(BusinessType.SignIn)) {
            BoostingDetail detail = boostingDetailMap.get(BusinessType.SignIn);
            String target = detail.getTarget();
            SignIn signIn = JSONUtil.toBean(detail.getProgress(), SignIn.class);
            // refresh target
            signIn.setDays(Integer.parseInt(target));
            signIn.setCreateTime(TimeUtils.timestampPlus8(detail.getCreateTime()));
            signIn.setLastUpdateTime(TimeUtils.timestampPlus8(detail.getLastUpdateTime()));
            progressOverview.setSignIn(signIn);
        }

        if (multiProgressMap.containsKey(BusinessType.Follower)) {
            List<MultiProgress> multiProgresses = multiProgressMap.get(BusinessType.Follower);
            List<Follower> followers = new ArrayList<>();
            multiProgresses.forEach(item -> {
                Follower follower = JSONUtil.toBean(item.getProgress(), Follower.class);
                follower.setCreateTime(TimeUtils.timestampPlus8(item.getCreateTime()));
                follower.setLastUpdateTime(TimeUtils.timestampPlus8(item.getLastUpdateTime()));
                String target = item.getTarget();
                String[] targetArr = target.split("\\|");
                follower.setLevelBreak(Boolean.parseBoolean(targetArr[0]));
                follower.setSkillBreak(Boolean.parseBoolean(targetArr[1]));
                follower.setHolyGrailChange(Boolean.parseBoolean(targetArr[2]));
                follower.setHolyGrailTotal(Integer.parseInt(targetArr[3]));
                followers.add(follower);
            });
            progressOverview.setFollower(followers);
        }

        if (multiProgressMap.containsKey(BusinessType.FollowerFetters)) {
            List<MultiProgress> multiProgresses = multiProgressMap.get(BusinessType.FollowerFetters);
            List<FollowerFetters> followerFetters = new ArrayList<>();
            multiProgresses.forEach(item -> {
                FollowerFetters fetters = JSONUtil.toBean(item.getProgress(), FollowerFetters.class);
                fetters.setCreateTime(TimeUtils.timestampPlus8(item.getCreateTime()));
                fetters.setLastUpdateTime(TimeUtils.timestampPlus8(item.getLastUpdateTime()));
                fetters.setFinalFettersLevel(Integer.parseInt(item.getTarget()));
                followerFetters.add(fetters);
            });
            progressOverview.setFollowerFetters(followerFetters);
        }

        if (multiProgressMap.containsKey(BusinessType.GatherMaterials)) {
            List<MultiProgress> multiProgresses = multiProgressMap.get(BusinessType.GatherMaterials);
            List<GatherMaterials> gatherMaterialsList = new ArrayList<>();
            multiProgresses.forEach(item -> {
                GatherMaterials gatherMaterials = JSONUtil.toBean(item.getProgress(), GatherMaterials.class);
                gatherMaterials.setCreateTime(TimeUtils.timestampPlus8(item.getCreateTime()));
                gatherMaterials.setLastUpdateTime(TimeUtils.timestampPlus8(item.getLastUpdateTime()));
                gatherMaterials.setTotal(Integer.parseInt(item.getTarget()));
                gatherMaterialsList.add(gatherMaterials);
            });
            progressOverview.setGatherMaterials(gatherMaterialsList);
        }

        if (multiProgressMap.containsKey(BusinessType.BoostingEvents)) {
            List<MultiProgress> multiProgresses = multiProgressMap.get(BusinessType.BoostingEvents);
            List<BoostingEvents> boostingEventsList = new ArrayList<>();
            multiProgresses.forEach(item -> {
                BoostingEvents boostingEvents = JSONUtil.toBean(item.getProgress(), BoostingEvents.class);
                boostingEvents.setCreateTime(TimeUtils.timestampPlus8(item.getCreateTime()));
                boostingEvents.setLastUpdateTime(TimeUtils.timestampPlus8(item.getLastUpdateTime()));
                boostingEventsList.add(boostingEvents);
            });
            progressOverview.setBoostingEvents(boostingEventsList);
        }

        if (multiProgressMap.containsKey(BusinessType.PurchaseLevels)) {
            List<MultiProgress> multiProgresses = multiProgressMap.get(BusinessType.PurchaseLevels);
            List<PurchaseLevels> purchaseLevels = new ArrayList<>();
            multiProgresses.forEach(item -> {
                PurchaseLevels purchaseLevel = JSONUtil.toBean(item.getProgress(), PurchaseLevels.class);
                purchaseLevel.setCreateTime(TimeUtils.timestampPlus8(item.getCreateTime()));
                purchaseLevel.setLastUpdateTime(TimeUtils.timestampPlus8(item.getLastUpdateTime()));
                purchaseLevels.add(purchaseLevel);
            });
            progressOverview.setPurchaseLevels(purchaseLevels);
        }
        if (boostingDetailMap.containsKey(BusinessType.GatherQP)) {
            BoostingDetail detail = boostingDetailMap.get(BusinessType.GatherQP);
            GatherQP gatherQP = JSONUtil.toBean(detail.getProgress(), GatherQP.class);
            gatherQP.setTotal(Integer.parseInt(detail.getTarget()));
            gatherQP.setCreateTime(TimeUtils.timestampPlus8(detail.getCreateTime()));
            gatherQP.setLastUpdateTime(TimeUtils.timestampPlus8(detail.getLastUpdateTime()));
            progressOverview.setGatherQP(gatherQP);
        }
        progressOverview.setBoostingLevels(new ArrayList<>());
        if (boostingDetailMap.containsKey(BusinessType.BoostingLevels)) {
            BoostingDetail detail = boostingDetailMap.get(BusinessType.BoostingLevels);
            List<BoostingLevels> boostingLevels = JSONUtil.toList(detail.getProgress(), BoostingLevels.class);
            BoostingLevels levels = boostingLevels.get(0);
            levels.setCreateTime(TimeUtils.timestampPlus8(detail.getCreateTime()));
            levels.setLastUpdateTime(TimeUtils.timestampPlus8(detail.getLastUpdateTime()));
            // todo 可能后面优化target
            progressOverview.getBoostingLevels().addAll(boostingLevels);
        }
        if (boostingDetailMap.containsKey(BusinessType.BoostingLevelsOfHunting)) {
            BoostingDetail detail = boostingDetailMap.get(BusinessType.BoostingLevelsOfHunting);
            List<BoostingLevels> boostingLevels = JSONUtil.toList(detail.getProgress(), BoostingLevels.class);
            BoostingLevels levels = boostingLevels.get(0);
            levels.setLastUpdateTime(TimeUtils.timestampPlus8(detail.getLastUpdateTime()));
            levels.setCreateTime(TimeUtils.timestampPlus8(detail.getCreateTime()));
            progressOverview.getBoostingLevels().addAll(boostingLevels);
        }
        if (boostingDetailMap.containsKey(BusinessType.BoostingLevelsOfStory)) {
            BoostingDetail detail = boostingDetailMap.get(BusinessType.BoostingLevelsOfStory);
            List<BoostingLevels> boostingLevels = JSONUtil.toList(detail.getProgress(), BoostingLevels.class);
            BoostingLevels levels = boostingLevels.get(0);
            levels.setLastUpdateTime(TimeUtils.timestampPlus8(detail.getLastUpdateTime()));
            levels.setCreateTime(TimeUtils.timestampPlus8(detail.getCreateTime()));
            progressOverview.getBoostingLevels().addAll(boostingLevels);
        }
        if (boostingDetailMap.containsKey(BusinessType.BoostingLevelsOfStrengthen)) {
            BoostingDetail detail = boostingDetailMap.get(BusinessType.BoostingLevelsOfStrengthen);
            List<BoostingLevels> boostingLevels = JSONUtil.toList(detail.getProgress(), BoostingLevels.class);
            BoostingLevels levels = boostingLevels.get(0);
            levels.setLastUpdateTime(TimeUtils.timestampPlus8(detail.getLastUpdateTime()));
            levels.setCreateTime(TimeUtils.timestampPlus8(detail.getCreateTime()));
            progressOverview.getBoostingLevels().addAll(boostingLevels);
        }
        if (boostingDetailMap.containsKey(BusinessType.BoostingLevelsOfPractice)) {
            BoostingDetail detail = boostingDetailMap.get(BusinessType.BoostingLevelsOfPractice);
            List<BoostingLevels> boostingLevels = JSONUtil.toList(detail.getProgress(), BoostingLevels.class);
            BoostingLevels levels = boostingLevels.get(0);
            levels.setLastUpdateTime(TimeUtils.timestampPlus8(detail.getLastUpdateTime()));
            levels.setCreateTime(TimeUtils.timestampPlus8(detail.getCreateTime()));
            progressOverview.getBoostingLevels().addAll(boostingLevels);
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

    public int updateToSyncFollower(long orderId) {
        return orderDetailMapper.updateToSyncFollower(orderId);
    }

    public String queryFollowerInfoByOrderId(@Param("id") long orderId) {
        return orderDetailMapper.queryFollowerInfoByOrderId(orderId);
    }
}

