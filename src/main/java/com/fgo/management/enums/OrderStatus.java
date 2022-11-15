package com.fgo.management.enums;

public enum OrderStatus {

    /**
     * 刚创建的订单
     */
    INIT,

    /**
     * 代练中
     */
    RUNNING,

    /**
     * 代练停止
     */
    STOPPED,

    /**
     * 已完成
     */
    FINISHED,
    /**
     * 未完成
     */
    UNFINISHED,
    /**
     * 活动结束未完成
     */
    UNFINISHED_EVENT_DONE,
    /**
     * 异常
     */
    ABNORMAL,
    /**
     * 密码异常
     */
    PASSWORD_EXCEPTION,
    /**
     * 体力不足
     */
    OUT_OF_ACTIVE_POWER,
    /**
     * 黑名单
     */
    BLACK_LIST,
    /**
     * 已结算
     */
    SETTLED,


}
