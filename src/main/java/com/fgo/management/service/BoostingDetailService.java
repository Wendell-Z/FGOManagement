package com.fgo.management.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.fgo.management.enums.BusinessType;
import com.fgo.management.enums.OperateType;
import com.fgo.management.mapper.BusinessDetailMapper;
import com.fgo.management.model.BoostingDetail;
import com.fgo.management.model.progress.GatherMaterials;
import com.fgo.management.model.progress.ProgressOverview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoostingDetailService {
    @Autowired
    private BusinessDetailMapper businessDetailMapper;
    @Autowired
    @Lazy
    private OrderDetailService orderDetailService;

    @Transactional
    public void merge(BoostingDetail boostingDetail) {
        long orderId = boostingDetail.getOrderId();
        String businessType = boostingDetail.getBusinessType();
        BusinessType type = BusinessType.valueOf(businessType);
        OperateType operateType = boostingDetail.getOperateType();
        if (type == BusinessType.BoostingEvents || type == BusinessType.Follower
                || type == BusinessType.FollowerFetters || type == BusinessType.GatherMaterials
                || type == BusinessType.PurchaseLevels) {
            BoostingDetail existed = businessDetailMapper.queryByOrderIdAndTypeWithLock(orderId, type);
            if (existed == null) {
                Timestamp initTime = new Timestamp(System.currentTimeMillis());
                boostingDetail.setCreateTime(initTime);
                boostingDetail.setStatus("N");
                boostingDetail.setLastUpdateTime(initTime);
                businessDetailMapper.merge(boostingDetail);
            } else {
                if (OperateType.ADD == operateType) {
                    String existedBoostingTask = existed.getBoostingTask();
                    String boostingTask = boostingDetail.getBoostingTask();
                    if (type == BusinessType.BoostingEvents || type == BusinessType.Follower || type == BusinessType.FollowerFetters) {
                        handleFormat1(boostingDetail, existed);
                    }
                    if (type == BusinessType.PurchaseLevels) {
                        handleFormat2(boostingDetail, existed);
                    }
                    if (type == BusinessType.GatherMaterials) {
                        handleFormat3(existed, boostingTask);
                    }
                    businessDetailMapper.update(existed);
                }
                if (OperateType.DELETE == operateType) {
                    if (BusinessType.GatherMaterials == type) {
                        removeByName(boostingDetail, existed);
                    } else {
                        removeByIndex(boostingDetail, existed);
                    }

                }

            }
        } else {
            Timestamp initTime = new Timestamp(System.currentTimeMillis());
            boostingDetail.setCreateTime(initTime);
            boostingDetail.setStatus("N");
            boostingDetail.setLastUpdateTime(initTime);
            businessDetailMapper.merge(boostingDetail);
        }
    }

    private void removeByName(BoostingDetail boostingDetail, BoostingDetail existed) {
        long orderId = boostingDetail.getOrderId();
        String businessType = boostingDetail.getBusinessType();
        // 匹配任务和进度 删除对应的即可
        String name = boostingDetail.getProgress();
        String existedBoostingTask = existed.getBoostingTask();
        String[] business = existedBoostingTask.split("\\|");
        List<String> leftBusiness = Arrays.stream(business).collect(Collectors.toList());
        leftBusiness.removeIf(item -> item.contains(name));
        if (leftBusiness.isEmpty()) {
            businessDetailMapper.delete(orderId, businessType);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < leftBusiness.size(); i++) {
                sb.append(leftBusiness.get(i));
                if (i < leftBusiness.size() - 1) {
                    sb.append("|");
                }
            }
            existed.setBoostingTask(sb.toString());
            // 进度也要删
            String progress = existed.getProgress();
            List<GatherMaterials> gatherMaterials = JSONUtil.toList(progress, GatherMaterials.class);
            gatherMaterials.removeIf(item -> item.getMaterialName().equals(name));
            existed.setProgress(JSONUtil.toJsonStr(gatherMaterials));
            businessDetailMapper.update(existed);
        }
    }

    private void removeByIndex(BoostingDetail boostingDetail, BoostingDetail existed) {
        long orderId = boostingDetail.getOrderId();
        String businessType = boostingDetail.getBusinessType();
        // 匹配任务和进度 删除对应的即可
        int index = Integer.parseInt(boostingDetail.getProgress());
        String existedBoostingTask = existed.getBoostingTask();
        String[] business = existedBoostingTask.split(",");
        List<String> leftBusiness = Arrays.stream(business).collect(Collectors.toList());
        leftBusiness.remove(index);
        if (leftBusiness.isEmpty()) {
            businessDetailMapper.delete(orderId, businessType);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < leftBusiness.size(); i++) {
                sb.append(leftBusiness.get(i));
                if (i < leftBusiness.size() - 1) {
                    sb.append(",");
                }
            }
            existed.setBoostingTask(sb.toString());
            // 进度也要删
            String progress = existed.getProgress();
            JSONArray objects = JSONUtil.parseArray(progress);
            objects.remove(index);
            existed.setProgress(JSONUtil.toJsonStr(objects));
            businessDetailMapper.update(existed);
        }
    }

    private void handleFormat3(BoostingDetail existed, String boostingTask) {
        String[] business = boostingTask.split("\\|");
        String progress = existed.getProgress();
        List<GatherMaterials> gatherMaterials = JSONUtil.toList(progress, GatherMaterials.class);
        List<GatherMaterials> newMaterialList = new ArrayList<>();
        Arrays.stream(business)
                .filter(materialName -> gatherMaterials
                        .stream()
                        .noneMatch(item -> item.getMaterialName().contains(materialName)))
                .forEach(item -> {
                    String[] items = item.split("=");
                    GatherMaterials material = new GatherMaterials();
                    material.setMaterialName(items[0]);
                    material.setTotal(Integer.parseInt(items[1]));
                    material.setStatus("N");
                    material.setActivePowerCost(0);
                    material.setCreateTime(new Timestamp(System.currentTimeMillis()));
                    material.setLastUpdateTime(new Timestamp(0));
                    material.setDoneCount(0);
                    newMaterialList.add(material);
                });
        gatherMaterials.addAll(newMaterialList);
        existed.setProgress(JSONUtil.toJsonStr(gatherMaterials));
        existed.setStatus("N");
        existed.setBoostingTask(boostingTask);
    }

    private void handleFormat2(BoostingDetail boostingDetail, BoostingDetail existed) {
        String boostingTask = boostingDetail.getBoostingTask();
        String existedBoostingTask = existed.getBoostingTask();
        // 单个
        String[] inBusiness = boostingTask.split("\\|");
        String[] business = existedBoostingTask.split(",");
        // 多个
        boolean isNew = true;
        for (int i = 0, businessLength = business.length; i < businessLength; i++) {
            String item = business[i];
            String[] littleItem = item.split("\\|");
            if (littleItem[0].equals(inBusiness[0])) {
                // 要替换
                business[i] = boostingTask;
                isNew = false;
            }
        }
        if (isNew) {
            appendBusiness(boostingDetail, existed, existedBoostingTask, boostingTask);
            existed.setStatus("N");
        }
    }

    private void appendBusiness(BoostingDetail boostingDetail, BoostingDetail existed, String existedBoostingTask, String boostingTask) {
        existedBoostingTask = existedBoostingTask + "," + boostingTask;
        String existedProgress = existed.getProgress();
        String progress = boostingDetail.getProgress();
        existedProgress = existedProgress.replace("\\", "");
        progress = progress.replace("\\", "");
        JSONArray jsonArray = JSONUtil.parseArray(existedProgress);
        jsonArray.add(JSONUtil.parseArray(progress).get(0));
        existed.setProgress(jsonArray.toString());
        existed.setBoostingTask(existedBoostingTask);
    }

    private void handleFormat1(BoostingDetail boostingDetail, BoostingDetail existed) {
        String boostingTask = boostingDetail.getBoostingTask();
        String existedBoostingTask = existed.getBoostingTask();
        // 单个
        String[] inBusiness = boostingTask.split("\\|");
        String[] business = existedBoostingTask.split(",");
        // 多个
        boolean isNew = true;
        for (int i = 0, businessLength = business.length; i < businessLength; i++) {
            String item = business[i];
            String[] littleItem = item.split("\\|");
            if (littleItem[0].equals(inBusiness[0]) && littleItem[1].equals(inBusiness[1])) {
                // 要替换
                business[i] = boostingTask;
                isNew = false;
            }
        }
        if (isNew) {
            appendBusiness(boostingDetail, existed, existedBoostingTask, boostingTask);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < business.length; i++) {
                sb.append(business[i]);
                if (i < business.length - 1) {
                    sb.append(",");
                }
            }
            existed.setBoostingTask(sb.toString());
        }
        existed.setStatus("N");
    }

    public void delete(long orderId, String businessType) {
        businessDetailMapper.delete(orderId, businessType);
    }

    public List<BoostingDetail> queryByOrderId(long orderId) {
        return businessDetailMapper.queryByOrderId(orderId);
    }

    public List<BoostingDetail> queryProgressByOrderId(long orderId) {
        return businessDetailMapper.queryProgressByOrderId(orderId);
    }

    public void updateProgress(List<BoostingDetail> boostingDetails) {
        businessDetailMapper.updateProgress(boostingDetails);
    }

    public BoostingDetail queryByOrderIdAndTypeWithLock(long orderId, BusinessType type) {
        return businessDetailMapper.queryByOrderIdAndTypeWithLock(orderId, type);
    }

    public ProgressOverview getFollowerInfo(long orderId) {
        String s = "{\n" +
                "    \"followerFetters\": [\n" +
                "        {\n" +
                "            \"profession\": \"saber\",\n" +
                "            \"followerName\": \"\",\n" +
                "            \"starLevel\": 0,\n" +
                "            \"fettersLevel\": 0,\n" +
                "            \"finalFettersLevel\": 0,\n" +
                "            \"status\": \"N\",\n" +
                "            \"createTime\": 0,\n" +
                "            \"lastUpdateTime\": \"\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"follower\": [\n" +
                "        {\n" +
                "            \"profession\": \"saber\",\n" +
                "            \"followerName\": \"saber\",\n" +
                "            \"starLevel\": 0,\n" +
                "            \"comeAgainLevel\": 0,\n" +
                "            \"followerLevel\": 0,\n" +
                "            \"skill1Level\": 0,\n" +
                "            \"skill2Level\": 0,\n" +
                "            \"skill3Level\": 0,\n" +
                "            \"levelBreak\": true,\n" +
                "            \"skillBreak\": true,\n" +
                "            \"holyGrailChange\": true,\n" +
                "            \"holyGrailTotal\": 111,\n" +
                "            \"status\": \"N\",\n" +
                "            \"createTime\": 0,\n" +
                "            \"lastUpdateTime\": \"\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        return JSONUtil.toBean(s, ProgressOverview.class);

//        // 先改状态 为SYNC
//        int rows = orderDetailService.updateToSyncFollower(orderId);
//        if (rows == 1) {
//            // 改成功后 直接不停扫描
//            String followerInfo = orderDetailService.queryFollowerInfoByOrderId(orderId);
//            int retry = 0;
//            while (StrUtil.isBlank(followerInfo) && retry <= 30) {
//                try {
//                    Thread.sleep(1000);
//                    retry++;
//                    followerInfo = orderDetailService.queryFollowerInfoByOrderId(orderId);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (StrUtil.isBlank(followerInfo)) {
//                throw new RuntimeException("查询超时请重试！");
//            } else {
//                return JSONUtil.toBean(followerInfo, ProgressOverview.class);
//            }
//        } else {
//            throw new RuntimeException("查询失败！请返回主页重试！");
//        }
    }

    public int queryUnfinishedBoosting(long orderId) {
        try {
            return businessDetailMapper.queryUnfinishedBoosting(orderId);
        } catch (Exception e) {
            throw new RuntimeException("查询未完成的代练任务失败!");
        }
    }
}
