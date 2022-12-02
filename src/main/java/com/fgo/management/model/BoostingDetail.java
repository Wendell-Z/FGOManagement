package com.fgo.management.model;

import com.fgo.management.enums.OperateType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
public class BoostingDetail {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotBlank(message = "业务类型不能为空！")
    private String businessType;

    private String status;

    @NotBlank(message = "代练内容不能为空！")
    private String boostingTask;

    @NotBlank(message = "进度不能为空！")
    private String progress;

    private Timestamp createTime;

    private Timestamp lastUpdateTime;

    /**
     * ADD UPDATE 单条 也得查询遍历 判断 才能知道是增 还是改
     * DELETE 我想让他传整体 这样我直接覆盖就行
     */
    private OperateType operateType;

    @NotBlank(message = "代练目标不能为空！")
    private String target;

    private Timestamp modifyTime;
}
