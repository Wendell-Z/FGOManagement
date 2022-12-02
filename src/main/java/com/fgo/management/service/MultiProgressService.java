package com.fgo.management.service;

import com.fgo.management.dto.KeyTarget;
import com.fgo.management.enums.BusinessType;
import com.fgo.management.mapper.MultiProgressMapper;
import com.fgo.management.model.MultiProgress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class MultiProgressService {

    @Autowired
    private MultiProgressMapper multiProgressMapper;

    public void updateProgress(List<MultiProgress> multiProgressList) {
        multiProgressMapper.updateProgress(multiProgressList);
    }

    public void merge(List<MultiProgress> multiProgressList) {
        if (multiProgressList.isEmpty()) {
            return;
        }
        multiProgressMapper.merge(multiProgressList);
    }

    public void deleteBusiness(long orderId, BusinessType type) {
        multiProgressMapper.deleteBusiness(orderId, type);
    }

    public void deleteKey(long orderId, BusinessType type, List<String> keys) {
        if (keys.isEmpty()) {
            return;
        }
        multiProgressMapper.deleteKey(orderId, type, keys);
    }

    public void updateTarget(long orderId, BusinessType type, List<KeyTarget> keyTargets, Timestamp modifyTime) {
        if (keyTargets.isEmpty()) {
            return;
        }
        multiProgressMapper.updateTarget(orderId, type, keyTargets, modifyTime);
    }

    public List<MultiProgress> queryProgressByOrderId(long orderId) {
        return multiProgressMapper.queryProgressByOrderId(orderId);
    }
}
