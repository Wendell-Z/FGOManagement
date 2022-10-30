package com.fgo.management.dto;

import lombok.Data;

@Data
public class QueryOrderCondition {

    private int pageNum = 1;

    private int pageSize = 20;

    private String queryStr;

    private String orderBy;

    private String status;

}
