package com.fgo.management.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ParamConfigMapper {

    void updateParamValue(@Param("root") String rootParam, @Param("sub") String subParam, @Param("value") String paramValue);
}
