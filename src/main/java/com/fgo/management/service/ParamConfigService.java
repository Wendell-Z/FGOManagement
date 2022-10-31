package com.fgo.management.service;

import com.fgo.management.mapper.ParamConfigMapper;
import com.fgo.management.model.ParamConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParamConfigService {


    @Autowired
    private ParamConfigMapper paramConfigMapper;

    public void updateParamValue(String rootParam, String subParam, String paramValue) {
        paramConfigMapper.updateParamValue(rootParam, subParam, paramValue);
    }

    public ParamConfig queryByParam(String root, String sub) {
        return paramConfigMapper.queryByParam(root, sub);
    }
}
