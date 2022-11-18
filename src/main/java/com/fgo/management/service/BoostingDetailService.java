package com.fgo.management.service;

import com.fgo.management.mapper.BusinessDetailMapper;
import com.fgo.management.model.BoostingDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class BoostingDetailService {

    @Autowired
    private BusinessDetailMapper businessDetailMapper;

    public void merge(BoostingDetail boostingDetail) {
        Timestamp initTime = new Timestamp(System.currentTimeMillis());
        boostingDetail.setCreateTime(initTime);
        boostingDetail.setStatus("N");
        boostingDetail.setLastUpdateTime(initTime);
        businessDetailMapper.merge(boostingDetail);
    }

    public void delete(long orderId, String businessType) {
        // 直接delete
        businessDetailMapper.delete(orderId, businessType);
    }

    public List<BoostingDetail> queryByOrderId(long orderId) {
        return businessDetailMapper.queryByOrderId(orderId);
    }

    public List<BoostingDetail> queryProgressByOrderId(long orderId) {
        return businessDetailMapper.queryProgressByOrderId(orderId);
    }
}
