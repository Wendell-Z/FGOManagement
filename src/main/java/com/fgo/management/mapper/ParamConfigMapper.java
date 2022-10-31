package com.fgo.management.mapper;

import com.fgo.management.model.ParamConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ParamConfigMapper {

    void updateParamValue(@Param("root") String rootParam, @Param("sub") String subParam, @Param("value") String paramValue);

    ParamConfig queryByParam(@Param("root") String root, @Param("sub") String sub);
}
