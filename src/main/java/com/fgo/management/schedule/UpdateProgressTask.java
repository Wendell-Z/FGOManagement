package com.fgo.management.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fgo.management.enums.BusinessType;
import com.fgo.management.enums.OrderStatus;
import com.fgo.management.model.BoostingDetail;
import com.fgo.management.model.OrderDetail;
import com.fgo.management.model.progress.*;
import com.fgo.management.service.BoostingDetailService;
import com.fgo.management.service.OrderDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Scheduled(initialDelay = 1000, fixedDelay = 1000)
    public void updateProgressTask() {
        LOGGER.info("update progress");
        // 查出来进度数据
        List<OrderDetail> orderDetailList = orderDetailService.queryByUpdateStatus("U");
        orderDetailList.forEach(item -> {
            try {
                updateProgressTask.handleProgress(item);
            } catch (Exception e) {
                // ignored
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
        handleSingle(orderId, progressOverview);
        boolean success = handleMulti(orderId, progressOverview);
        if (success) {
            orderDetail.setUpdateStatus("N");
        } else {
            orderDetail.setUpdateStatus("U");
        }
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

    private void handleSingle(long orderId, ProgressOverview progressOverview) {
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
                // 可以直接更新
                boostingDetails.add(detail);
            }
        }
        boostingDetailService.updateProgress(boostingDetails);
    }

    private boolean handleMulti(long orderId, ProgressOverview progressOverview) {
        boolean success = true;
        List<Follower> followers = progressOverview.getFollower();
        if (!CollectionUtil.isEmpty(followers)) {
            try {
                updateProgressTask.handleFollowers(orderId, followers);
            } catch (Exception e) {
                LOGGER.error("更新从者失败！{}", e.getMessage());
                success = false;
            }
        }
        List<FollowerFetters> followerFetters = progressOverview.getFollowerFetters();
        if (!CollectionUtil.isEmpty(followerFetters)) {
            try {
                updateProgressTask.handleFollowerFetters(orderId, followerFetters);
            } catch (Exception e) {
                LOGGER.error("更新从者羁绊失败！{}", e.getMessage());
                success = false;
            }
        }

        List<GatherMaterials> gatherMaterials = progressOverview.getGatherMaterials();
        if (!CollectionUtil.isEmpty(gatherMaterials)) {
            try {
                updateProgressTask.handleMaterials(orderId, gatherMaterials);
            } catch (Exception e) {
                LOGGER.error("更新材料失败！{}", e.getMessage());
                success = false;
            }
        }

        List<PurchaseLevels> purchaseLevels = progressOverview.getPurchaseLevels();
        if (!CollectionUtil.isEmpty(purchaseLevels)) {
            try {
                updateProgressTask.handlePurchaseLevels(orderId, purchaseLevels);
            } catch (Exception e) {
                LOGGER.error("更新购买关卡失败！{}", e.getMessage());
                success = false;
            }
        }

        List<BoostingEvents> boostingEvents = progressOverview.getBoostingEvents();
        if (!CollectionUtil.isEmpty(boostingEvents)) {
            try {
                updateProgressTask.handleEvents(orderId, boostingEvents);
            } catch (Exception e) {
                LOGGER.error("更新活动代练失败！{}", e.getMessage());
                success = false;
            }
        }
        return success;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleEvents(long orderId, List<BoostingEvents> boostingEvents) {
        BoostingDetail existed = boostingDetailService.queryByOrderIdAndTypeWithLock(orderId, BusinessType.BoostingEvents);
        if (existed != null) {
            String progress = existed.getProgress();
            List<BoostingEvents> existedBoostingEvents = JSONUtil.toList(progress, BoostingEvents.class);
            //更新已有的
            existedBoostingEvents.forEach(eventItem -> {
                Optional<BoostingEvents> any = boostingEvents
                        .stream()
                        .filter(event -> event.getEventName()
                                .equals(eventItem.getEventName()))
                        .findAny();
                if (any.isPresent()) {
                    BoostingEvents anyItem = any.get();
                    BeanUtils.copyProperties(anyItem, eventItem);
                }
            });
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePurchaseLevels(long orderId, List<PurchaseLevels> purchaseLevels) {
        BoostingDetail existed = boostingDetailService.queryByOrderIdAndTypeWithLock(orderId, BusinessType.PurchaseLevels);
        if (existed != null) {
            String progress = existed.getProgress();
            List<PurchaseLevels> existedPurchaseLevels = JSONUtil.toList(progress, PurchaseLevels.class);
            //更新已有的
            existedPurchaseLevels.forEach(purchaseLevelsItem -> {
                Optional<PurchaseLevels> any = purchaseLevels
                        .stream()
                        .filter(purchaseLevel -> purchaseLevel.getLevelName()
                                .equals(purchaseLevelsItem.getLevelName()))
                        .findAny();
                if (any.isPresent()) {
                    PurchaseLevels anyItem = any.get();
                    BeanUtils.copyProperties(anyItem, purchaseLevelsItem);
                }
            });
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleMaterials(long orderId, List<GatherMaterials> gatherMaterials) {
        BoostingDetail existed = boostingDetailService.queryByOrderIdAndTypeWithLock(orderId, BusinessType.GatherMaterials);
        if (existed != null) {
            String progress = existed.getProgress();
            List<GatherMaterials> existedGatherMaterials = JSONUtil.toList(progress, GatherMaterials.class);
            //更新已有的
            existedGatherMaterials.forEach(gatherMaterialItem -> {
                Optional<GatherMaterials> any = gatherMaterials
                        .stream()
                        .filter(material -> material.getMaterialName()
                                .equals(gatherMaterialItem.getMaterialName()))
                        .findAny();
                if (any.isPresent()) {
                    GatherMaterials anyItem = any.get();
                    BeanUtils.copyProperties(anyItem, gatherMaterialItem);
                }
            });
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFollowerFetters(long orderId, List<FollowerFetters> followerFetters) {
        BoostingDetail existed = boostingDetailService.queryByOrderIdAndTypeWithLock(orderId, BusinessType.FollowerFetters);
        if (existed != null) {
            String progress = existed.getProgress();
            List<FollowerFetters> existedFollowerFetters = JSONUtil.toList(progress, FollowerFetters.class);
            //更新已有的
            existedFollowerFetters.forEach(followerFettersItem -> {
                Optional<FollowerFetters> any = followerFetters
                        .stream()
                        .filter(syncFollowerFetters -> syncFollowerFetters.getFollowerName()
                                .equals(followerFettersItem.getFollowerName()))
                        .findAny();
                if (any.isPresent()) {
                    FollowerFetters anyItem = any.get();
                    BeanUtils.copyProperties(anyItem, followerFettersItem);
                }
            });
            // 追加新的
            followerFetters.removeAll(existedFollowerFetters);
            existedFollowerFetters.addAll(followerFetters);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFollowers(long orderId, List<Follower> followers) {
        BoostingDetail existed = boostingDetailService.queryByOrderIdAndTypeWithLock(orderId, BusinessType.Follower);
        if (existed != null) {
            String progress = existed.getProgress();
            List<Follower> existedFollowers = JSONUtil.toList(progress, Follower.class);
            //更新已有的
            existedFollowers.forEach(followerItem -> {
                Optional<Follower> any = followers
                        .stream()
                        .filter(follower -> follower.getFollowerName()
                                .equals(followerItem.getFollowerName()))
                        .findAny();
                if (any.isPresent()) {
                    Follower anyItem = any.get();
                    BeanUtils.copyProperties(anyItem, followerItem);
                }
            });
            // 追加新的
            followers.removeAll(existedFollowers);
            existedFollowers.addAll(followers);
        }
    }
}
