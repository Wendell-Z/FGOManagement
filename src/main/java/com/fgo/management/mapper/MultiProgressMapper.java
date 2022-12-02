package com.fgo.management.mapper;

import com.fgo.management.dto.KeyTarget;
import com.fgo.management.enums.BusinessType;
import com.fgo.management.model.MultiProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface MultiProgressMapper {


    void updateProgress(@Param("list") List<MultiProgress> multiProgressList);

    void merge(@Param("list") List<MultiProgress> multiProgressList);

    void deleteBusiness(@Param("orderId") long orderId, @Param("type") BusinessType type);

    void deleteKey(@Param("orderId") long orderId, @Param("type") BusinessType type, @Param("list") List<String> keys);

    void updateTarget(@Param("orderId") long orderId, @Param("type") BusinessType type, @Param("list") List<KeyTarget> keyTargets, @Param("modifyTime") Timestamp modifyTime);

    List<MultiProgress> queryProgressByOrderId(@Param("orderId") long orderId);
}
