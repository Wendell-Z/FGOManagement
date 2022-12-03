package com.fgo.management.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.fgo.management.common.Constants;
import com.fgo.management.dto.KeyTarget;
import com.fgo.management.enums.BusinessType;
import com.fgo.management.enums.OperateType;
import com.fgo.management.mapper.BusinessDetailMapper;
import com.fgo.management.model.BoostingDetail;
import com.fgo.management.model.MultiProgress;
import com.fgo.management.model.progress.GatherMaterials;
import com.fgo.management.model.progress.ProgressOverview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BoostingDetailService {
    @Autowired
    private BusinessDetailMapper businessDetailMapper;
    @Autowired
    private MultiProgressService multiProgressService;

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
            String boostingTask = boostingDetail.getBoostingTask();
            Timestamp initTime = new Timestamp(System.currentTimeMillis());
            if (existed == null) {
                if (OperateType.ADD == operateType) {
                    boostingDetail.setCreateTime(initTime);
                    boostingDetail.setStatus("N");
                    boostingDetail.setLastUpdateTime(initTime);
                    businessDetailMapper.merge(boostingDetail);
                    if (type == BusinessType.BoostingEvents || type == BusinessType.Follower || type == BusinessType.FollowerFetters
                            || type == BusinessType.PurchaseLevels) {
                        String businessKey = boostingTask;
                        if (type == BusinessType.Follower || type == BusinessType.FollowerFetters) {
                            // 职业=saber|名称=saber|星级=0|再临=0|等级=0|技能1=0|技能2=0|技能3=0|等级满破=true|技能满破=true|圣杯转临=111|从者及从者技能满破
                            String[] split = businessKey.split("\\|");
                            businessKey = split[0].split("=")[1] + "|" + split[1].split("=")[1];
                        }
                        if (type == BusinessType.PurchaseLevels) {
                            // 关卡=金色庆典获取任务|代练内容=活动毕业|购买关卡
                            String[] items = businessKey.split("\\|");
                            businessKey = items[0].split("=")[1] + "|" + items[1].split("=")[1];
                        }
                        if (type == BusinessType.BoostingEvents) {
                            String[] items = boostingTask.split("\\|");
                            String name = items[0].split("=")[1];
                            String content = items[1].split("=")[1];
                            businessKey = name + "|" + content;
                        }
                        String progress = boostingDetail.getProgress();
                        progress = progress.replace("\\", "");
                        progress = JSONUtil.parseArray(progress).getJSONObject(0).toString();
                        MultiProgress multiProgress = new MultiProgress();
                        multiProgress.setProgress(progress);
                        multiProgress.setBusinessKey(businessKey);
                        multiProgress.setBusinessType(type.name());
                        multiProgress.setOrderId(orderId);
                        multiProgress.setCreateTime(initTime);
                        multiProgress.setLastUpdateTime(initTime);
                        multiProgress.setStatus("N");
                        multiProgress.setTarget(boostingDetail.getTarget());
                        multiProgressService.merge(List.of(multiProgress));
                    }
                    if (type == BusinessType.GatherMaterials) {
                        //  凤凰羽毛=1|无间齿轮=1|素材
                        String[] materialSettings = boostingTask.split("\\|");
                        List<MultiProgress> multiProgressList = new ArrayList<>(materialSettings.length);
                        for (int i = 0; i < materialSettings.length; i++) {
                            if (i == materialSettings.length - 1) {
                                continue;
                            }
                            String[] setting = materialSettings[i].split("=");
                            String name = setting[0];
                            String target = setting[1];
                            GatherMaterials gatherMaterials = new GatherMaterials();
                            gatherMaterials.setMaterialName(name);
                            gatherMaterials.setCreateTime(initTime);
                            gatherMaterials.setStatus("N");
                            gatherMaterials.setTotal(Integer.parseInt(target));
                            gatherMaterials.setDoneCount(0);
                            gatherMaterials.setActivePowerCost(0);
                            gatherMaterials.setLastUpdateTime(initTime);
                            String progress = JSONUtil.toJsonStr(gatherMaterials);
                            MultiProgress multiProgress = constructWithoutTarget(orderId, type, initTime, name, progress);
                            multiProgress.setTarget(target);
                            multiProgressList.add(multiProgress);
                        }
                        multiProgressService.merge(multiProgressList);
                    }
                }
                if (OperateType.DELETE == operateType) {
                    multiProgressService.deleteBusiness(orderId, type);
                }
            } else {
                existed.setModifyTime(initTime);
                if (OperateType.ADD == operateType) {
                    List<KeyTarget> keyTargets = new ArrayList<>();
                    if (type == BusinessType.BoostingEvents || type == BusinessType.Follower || type == BusinessType.FollowerFetters) {
                        String[] items = boostingTask.split("\\|");
                        keyTargets.add(new KeyTarget(items[0].split("=")[1] + "|" + items[1].split("=")[1], boostingDetail.getTarget()));
                        handleFormat1(boostingDetail, existed, initTime);
                    }
                    if (type == BusinessType.PurchaseLevels) {
                        handleFormat2(boostingDetail, existed, initTime);
                    }
                    if (type == BusinessType.GatherMaterials) {
                        String[] settings = boostingTask.split("\\|");
                        for (int i = 0; i < settings.length; i++) {
                            if (i == settings.length - 1) {
                                continue;
                            }
                            String[] setting = settings[i].split("=");
                            keyTargets.add(new KeyTarget(setting[0], setting[1]));
                        }
                        handleFormat3(existed, boostingTask, initTime);
                    }
                    multiProgressService.updateTarget(orderId, type, keyTargets, initTime);
                    businessDetailMapper.update(existed);
                }
                if (OperateType.DELETE == operateType) {
                    List<String> keys = new ArrayList<>();
                    if (BusinessType.GatherMaterials == type) {
                        keys.add(boostingDetail.getProgress());
                        removeByName(boostingDetail, existed);
                    } else {
                        keys.add(boostingDetail.getProgress());
                        removeByIndex(boostingDetail, existed);
                    }
                    multiProgressService.deleteKey(orderId, type, keys);
                }
            }
        } else {
            Timestamp initTime = new Timestamp(System.currentTimeMillis());
            boostingDetail.setCreateTime(initTime);
            boostingDetail.setStatus("N");
            boostingDetail.setLastUpdateTime(initTime);
            boostingDetail.setModifyTime(initTime);
            businessDetailMapper.merge(boostingDetail);
        }
    }

    private MultiProgress constructWithoutTarget(long orderId, BusinessType type, Timestamp initTime, String businessKey, String progress) {
        MultiProgress multiProgress = new MultiProgress();
        multiProgress.setProgress(progress);
        multiProgress.setBusinessKey(businessKey);
        multiProgress.setBusinessType(type.name());
        multiProgress.setOrderId(orderId);
        multiProgress.setCreateTime(initTime);
        multiProgress.setLastUpdateTime(initTime);
        multiProgress.setStatus("N");
        return multiProgress;
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

    private void handleFormat3(BoostingDetail existed, String boostingTask, Timestamp timestamp) {
        String[] business = boostingTask.split("\\|");
        List<String> taskList = Arrays.asList(business);
        String existedBoostingTask = existed.getBoostingTask();
        List<String> existedList = new ArrayList<>(Arrays.asList(existedBoostingTask.split("\\|")));
        List<MultiProgress> multiProgressList = new ArrayList<>();
        taskList.forEach(item -> {
            if ("素材".equals(item)) {
                return;
            }
            String[] split = item.split("=");
            String name = split[0];
            String target = split[1];
            Optional<String> any = existedList.stream().filter(task -> task.contains(name)).findAny();
            if (any.isPresent()) {
                String s = any.get();
                existedList.remove(s);
                existedList.add(existedList.size() - 1, item);

            } else {
                existedList.add(existedList.size() - 1, item);
                MultiProgress multiProgress = new MultiProgress();
                multiProgress.setOrderId(existed.getOrderId());
                multiProgress.setModifyTime(timestamp);
                multiProgress.setCreateTime(timestamp);
                multiProgress.setLastUpdateTime(timestamp);
                multiProgress.setStatus("N");
                multiProgress.setBusinessKey(name);
                multiProgress.setBusinessType(BusinessType.GatherMaterials.name());
                multiProgress.setTarget(target);
                GatherMaterials gatherMaterials = new GatherMaterials();
                gatherMaterials.setTotal(Integer.parseInt(target));
                gatherMaterials.setCreateTime(timestamp);
                gatherMaterials.setLastUpdateTime(timestamp);
                gatherMaterials.setStatus("N");
                gatherMaterials.setMaterialName(name);
                gatherMaterials.setActivePowerCost(0);
                gatherMaterials.setDoneCount(0);
                multiProgress.setProgress(JSONUtil.toJsonStr(gatherMaterials));
                multiProgressList.add(multiProgress);
            }
        });
        existed.setStatus("N");
        StringBuilder sb = new StringBuilder();
        existedList.forEach(item -> sb.append(item).append("|"));
        sb.delete(sb.length() - 1, sb.length());
        existed.setBoostingTask(sb.toString());
        if (!multiProgressList.isEmpty()) {
            multiProgressService.merge(multiProgressList);
        }
    }


    private void appendBusiness(BoostingDetail existed, String existedBoostingTask, String boostingTask) {
        existedBoostingTask = existedBoostingTask + "," + boostingTask;
        existed.setBoostingTask(existedBoostingTask);
    }

    private void handleFormat2(BoostingDetail boostingDetail, BoostingDetail existed, Timestamp timestamp) {
        String boostingTask = boostingDetail.getBoostingTask();
        String existedBoostingTask = existed.getBoostingTask();
        String[] business = existedBoostingTask.split(",");
        boolean noneMatch = Arrays.stream(business).noneMatch(item -> item.equals(boostingTask));
        if (noneMatch) {
            existedBoostingTask = existedBoostingTask + "," + boostingTask;
            existed.setBoostingTask(existedBoostingTask);
            existed.setStatus("N");
            MultiProgress multiProgress = new MultiProgress();
            multiProgress.setOrderId(existed.getOrderId());
            multiProgress.setModifyTime(timestamp);
            multiProgress.setCreateTime(timestamp);
            multiProgress.setLastUpdateTime(timestamp);
            multiProgress.setStatus("N");
            multiProgress.setBusinessKey(boostingTask);
            multiProgress.setBusinessType(BusinessType.PurchaseLevels.name());
            multiProgress.setTarget(Constants.EMPTY_STRING);
            multiProgress.setProgress(JSONUtil.parseArray(boostingDetail.getProgress()).getJSONObject(0).toString());
            multiProgressService.merge(Collections.singletonList(multiProgress));
        }
    }

    private void handleFormat1(BoostingDetail boostingDetail, BoostingDetail existed, Timestamp timestamp) {
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
            appendBusiness(existed, existedBoostingTask, boostingTask);
            MultiProgress multiProgress = new MultiProgress();
            multiProgress.setOrderId(boostingDetail.getOrderId());
            multiProgress.setStatus("N");
            multiProgress.setBusinessType(boostingDetail.getBusinessType());
            multiProgress.setCreateTime(timestamp);
            multiProgress.setLastUpdateTime(timestamp);
            multiProgress.setTarget(Constants.EMPTY_STRING);
            multiProgress.setBusinessKey(inBusiness[0].split("=")[1] + "|" + inBusiness[1].split("=")[1]);
            multiProgress.setModifyTime(timestamp);
            multiProgress.setProgress(JSONUtil.parseArray(boostingDetail.getProgress()).getJSONObject(0).toString());
            multiProgressService.merge(Collections.singletonList(multiProgress));
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
                "            \"followerName\": \"saber\",\n" +
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
