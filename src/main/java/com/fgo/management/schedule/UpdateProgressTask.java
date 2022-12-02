package com.fgo.management.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fgo.management.enums.BusinessType;
import com.fgo.management.enums.OrderStatus;
import com.fgo.management.model.BoostingDetail;
import com.fgo.management.model.MultiProgress;
import com.fgo.management.model.OrderDetail;
import com.fgo.management.model.progress.*;
import com.fgo.management.service.BoostingDetailService;
import com.fgo.management.service.MultiProgressService;
import com.fgo.management.service.OrderDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
public class UpdateProgressTask {

    public static final Logger LOGGER = LoggerFactory.getLogger(LoginCacheClearTask.class);

    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private BoostingDetailService boostingDetailService;
    @Autowired
    @Lazy
    private UpdateProgressTask updateProgressTask;
    @Autowired
    private MultiProgressService multiProgressService;

    @Scheduled(initialDelay = 1000, fixedDelay = 1000)
    public void updateProgressTask() {
        LOGGER.info("update progress");
        // 查出来进度数据
        List<OrderDetail> orderDetailList = orderDetailService.queryByUpdateStatus("U");
        orderDetailList.forEach(item -> {
            long id = item.getId();
            try {
                updateProgressTask.handleProgress(item);
            } catch (Exception e) {
                LOGGER.error("刷新进度失败！订单ID:{}异常:{}", id, e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleProgress(OrderDetail item) {
        long orderId = item.getId();
        String boostingProgress = item.getBoostingProgress();
        // 订单本身的信息更新
        OrderDetail orderDetail = JSONUtil.toBean(boostingProgress, OrderDetail.class);
        orderDetail.setId(orderId);
        ProgressOverview progressOverview = JSONUtil.toBean(boostingProgress, ProgressOverview.class);
        Timestamp modifyTime = new Timestamp(System.currentTimeMillis());
        handleSingle(orderId, progressOverview, modifyTime);
        handleMulti(orderId, progressOverview, modifyTime);
        orderDetail.setUpdateStatus("N");
        // 更新某个业务的状态
        // 查询该订单所有的状态
        if (!StrUtil.isBlank(orderDetail.getExceptionMessage())) {
            // 活动结束未完成 异常 密码问题 AP不足 黑名单
            orderDetail.setOrderStatus(OrderStatus.ABNORMAL);
            orderDetail.setStatus(OrderStatus.STOPPED);
        } else {
            // 已完成 查询该订单下所有进度 是否是非未完成的就行
            int count = boostingDetailService.queryUnfinishedBoosting(orderId);
            if (count == 0) {
                orderDetail.setOrderStatus(OrderStatus.FINISHED);
            }
        }
        orderDetailService.updateProgress(orderDetail);
    }

    private void handleSingle(long orderId, ProgressOverview progressOverview, Timestamp modifyTime) {
        long refreshTime = progressOverview.getRefreshTime();
        Timestamp refreshTimestamp = new Timestamp(refreshTime);
        List<BoostingDetail> boostingDetails = new ArrayList<>();
        GatherQP gatherQP = progressOverview.getGatherQP();
        if (gatherQP != null) {
            // 直接覆盖
            BoostingDetail detail = new BoostingDetail();
            detail.setOrderId(orderId);
            detail.setLastUpdateTime(refreshTimestamp);
            detail.setProgress(JSONUtil.toJsonStr(gatherQP));
            detail.setBusinessType(BusinessType.GatherQP.name());
            detail.setStatus(gatherQP.getStatus());
            detail.setModifyTime(modifyTime);
            // 可以直接更新
            boostingDetails.add(detail);
        }
        Daily daily = progressOverview.getDaily();
        if (daily != null) {
            // 直接覆盖
            BoostingDetail detail = new BoostingDetail();
            detail.setOrderId(orderId);
            detail.setLastUpdateTime(refreshTimestamp);
            detail.setProgress(JSONUtil.toJsonStr(daily));
            detail.setBusinessType(BusinessType.Daily.name());
            detail.setBoostingTask(daily.getStatus());
            detail.setModifyTime(modifyTime);
            // 可以直接更新
            boostingDetails.add(detail);
        }
        GatherGreenSquare gatherGreenSquare = progressOverview.getGatherGreenSquare();
        if (gatherGreenSquare != null) {
            // 直接覆盖
            BoostingDetail detail = new BoostingDetail();
            detail.setOrderId(orderId);
            detail.setLastUpdateTime(refreshTimestamp);
            detail.setProgress(JSONUtil.toJsonStr(daily));
            detail.setBusinessType(BusinessType.GatherGreenSquare.name());
            detail.setStatus(gatherGreenSquare.getStatus());
            detail.setModifyTime(modifyTime);
            // 可以直接更新
            boostingDetails.add(detail);
        }
        GatherDogFood gatherDogFood = progressOverview.getGatherDogFood();
        if (gatherDogFood != null) {
            // 直接覆盖
            BoostingDetail detail = new BoostingDetail();
            detail.setOrderId(orderId);
            detail.setLastUpdateTime(refreshTimestamp);
            detail.setProgress(JSONUtil.toJsonStr(gatherDogFood));
            detail.setBusinessType(BusinessType.GatherDogFood.name());
            detail.setStatus(gatherDogFood.getStatus());
            detail.setModifyTime(modifyTime);
            // 可以直接更新
            boostingDetails.add(detail);
        }
        GatherBalls gatherBalls = progressOverview.getGatherBalls();
        if (gatherBalls != null) {
            // 直接覆盖
            BoostingDetail detail = new BoostingDetail();
            detail.setOrderId(orderId);
            detail.setLastUpdateTime(refreshTimestamp);
            detail.setProgress(JSONUtil.toJsonStr(gatherBalls));
            detail.setBusinessType(BusinessType.GatherBalls.name());
            detail.setStatus(gatherBalls.getStatus());
            detail.setModifyTime(modifyTime);
            // 可以直接更新
            boostingDetails.add(detail);
        }
        SignIn signIn = progressOverview.getSignIn();
        if (signIn != null) {
            // 直接覆盖
            BoostingDetail detail = new BoostingDetail();
            detail.setOrderId(orderId);
            detail.setLastUpdateTime(refreshTimestamp);
            detail.setProgress(JSONUtil.toJsonStr(signIn));
            detail.setBusinessType(BusinessType.SignIn.name());
            detail.setStatus(signIn.getStatus());
            detail.setModifyTime(modifyTime);
            // 可以直接更新
            boostingDetails.add(detail);
        }
        List<BoostingLevels> boostingLevels = progressOverview.getBoostingLevels();
        if (!CollectionUtil.isEmpty(boostingLevels)) {
            for (BoostingLevels levels : boostingLevels) {
                String type = levels.getType();
                // 直接覆盖
                BoostingDetail detail = new BoostingDetail();
                detail.setOrderId(orderId);
                detail.setLastUpdateTime(refreshTimestamp);
                detail.setProgress(JSONUtil.toJsonStr(levels));
                detail.setBusinessType(type);
                detail.setStatus(levels.getStatus());
                detail.setModifyTime(modifyTime);
                // 可以直接更新
                boostingDetails.add(detail);
            }
        }
        if (!boostingDetails.isEmpty()) {
            boostingDetailService.updateProgress(boostingDetails);
        }
    }

    private void handleMulti(long orderId, ProgressOverview progressOverview, Timestamp modifyTime) {
        List<MultiProgress> multiProgressList = new ArrayList<>();
        List<Follower> followers = progressOverview.getFollower();
        updateProgressTask.handleFollowers(orderId, followers, multiProgressList);
        List<FollowerFetters> followerFetters = progressOverview.getFollowerFetters();
        updateProgressTask.handleFollowerFetters(orderId, followerFetters, multiProgressList);
        List<GatherMaterials> gatherMaterials = progressOverview.getGatherMaterials();
        updateProgressTask.handleMaterials(orderId, gatherMaterials, multiProgressList);
        List<PurchaseLevels> purchaseLevels = progressOverview.getPurchaseLevels();
        updateProgressTask.handlePurchaseLevels(orderId, purchaseLevels, multiProgressList);
        List<BoostingEvents> boostingEvents = progressOverview.getBoostingEvents();
        updateProgressTask.handleEvents(orderId, boostingEvents, multiProgressList);
        if (!multiProgressList.isEmpty()) {
            multiProgressList.forEach(item -> item.setModifyTime(modifyTime));
            multiProgressService.updateProgress(multiProgressList);
        }
    }


    public void handleEvents(long orderId, List<BoostingEvents> boostingEvents, List<MultiProgress> multiProgressList) {
        boostingEvents.forEach(item -> {
            String name = item.getEventName();
            String progress = JSONUtil.toJsonStr(item);
            MultiProgress multiProgress = new MultiProgress();
            multiProgress.setOrderId(orderId);
            multiProgress.setProgress(progress);
            multiProgress.setBusinessKey(name);
            multiProgress.setStatus(item.getStatus());
            // todo 如果他没有 就写本地时间
            multiProgress.setLastUpdateTime(item.getLastUpdateTime());
            multiProgressList.add(multiProgress);
        });
    }

    public void handlePurchaseLevels(long orderId, List<PurchaseLevels> purchaseLevels, List<MultiProgress> multiProgressList) {
        purchaseLevels.forEach(item -> {
            String name = item.getLevelName();
            String progress = JSONUtil.toJsonStr(item);
            MultiProgress multiProgress = new MultiProgress();
            multiProgress.setOrderId(orderId);
            multiProgress.setProgress(progress);
            multiProgress.setBusinessKey(name);
            multiProgress.setStatus(item.getStatus());
            // todo 如果他没有 就写本地时间
            multiProgress.setLastUpdateTime(item.getLastUpdateTime());
            multiProgressList.add(multiProgress);
        });
    }

    public void handleMaterials(long orderId, List<GatherMaterials> gatherMaterials, List<MultiProgress> multiProgressList) {
        gatherMaterials.forEach(item -> {
            String name = item.getMaterialName();
            String progress = JSONUtil.toJsonStr(item);
            MultiProgress multiProgress = new MultiProgress();
            multiProgress.setOrderId(orderId);
            multiProgress.setProgress(progress);
            multiProgress.setBusinessKey(name);
            multiProgress.setStatus(item.getStatus());
            // todo 如果他没有 就写本地时间
            multiProgress.setLastUpdateTime(item.getLastUpdateTime());
            multiProgressList.add(multiProgress);
        });
    }

    public void handleFollowerFetters(long orderId, List<FollowerFetters> followerFetters, List<MultiProgress> multiProgressList) {
        followerFetters.forEach(item -> {
            String followerName = item.getFollowerName();
            String profession = item.getProfession();
            String progress = JSONUtil.toJsonStr(item);
            MultiProgress multiProgress = new MultiProgress();
            multiProgress.setOrderId(orderId);
            multiProgress.setProgress(progress);
            multiProgress.setBusinessKey(profession + "|" + followerName);
            multiProgress.setStatus(item.getStatus());
            // todo 如果他没有 就写本地时间
            multiProgress.setLastUpdateTime(item.getLastUpdateTime());
            multiProgressList.add(multiProgress);
        });
    }

    public void handleFollowers(long orderId, List<Follower> followers, List<MultiProgress> multiProgressList) {
        followers.forEach(item -> {
            String followerName = item.getFollowerName();
            String profession = item.getProfession();
            String progress = JSONUtil.toJsonStr(item);
            MultiProgress multiProgress = new MultiProgress();
            multiProgress.setOrderId(orderId);
            multiProgress.setProgress(progress);
            multiProgress.setBusinessKey(profession + "|" + followerName);
            multiProgress.setStatus(item.getStatus());
            // todo 如果他没有 就写本地时间
            multiProgress.setLastUpdateTime(item.getLastUpdateTime());
            multiProgressList.add(multiProgress);
        });
    }
}
