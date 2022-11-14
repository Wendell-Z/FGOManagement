package com.fgo.management.service;

import cn.hutool.json.JSONUtil;
import com.fgo.management.mapper.ParamConfigMapper;
import com.fgo.management.model.BusinessOrder;
import com.fgo.management.model.ParamConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public void setBusinessOrder(List<BusinessOrder> businessOrders) {
        paramConfigMapper.mergeBusinessOrder(JSONUtil.toJsonStr(businessOrders));
    }
}
