package com.fgo.management.mapper;

import com.fgo.management.model.BoostingDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BusinessDetailMapper {


    void merge(@Param("bean") BoostingDetail boostingDetail);

    void delete(@Param("orderId") long orderId, @Param("businessType") String businessType);

    List<BoostingDetail> queryByOrderId(@Param("orderId") long orderId);
}
