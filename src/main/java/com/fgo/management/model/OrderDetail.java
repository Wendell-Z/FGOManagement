package com.fgo.management.model;

import com.fgo.management.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetail {

    private long id;

    private String orderNumber;

    @NotBlank(message = "玩家账号不能为空！")
    private String playerAccount;

    @NotBlank(message = "玩家密码不能为空！")
    private String playerEncryptionCode;

    @NotBlank(message = "玩家渠道不能为空！")
    @Pattern(regexp = "Android|iOS", message = "玩家渠道只能为Android|iOS")
    private String playerChannel;

    @NotBlank(message = "梯队不动不能为空！")
    @Pattern(regexp = "TRUE|FALSE", message = "梯队不动只能为TRUE|FALSE")
    private String teamOnHold;

    private String boostingContent;

    /**
     * string 构造器 pattern
     */
    @Min(value = 0, message = "订单金额最小为0")
    private BigDecimal orderAmount;

    @NotBlank(message = "玩家联系方式不能为空！")
    private String playerContact;

    private OrderStatus status;

    private Timestamp createTime;
    private Timestamp recentlyLoginTime;


    private int rockAtStart;
    private int fruitAtStart;
    private int fruitAtCurrent;
    private int rockAtCurrent;
    private int battleCount;

    private String exceptionMessage;
    @NotBlank(message = "是否消耗苹果不能为空！")
    @Pattern(regexp = "TRUE|FALSE", message = "是否消耗苹果只能为TRUE|FALSE")
    private String fruitEnabled;
    @NotBlank(message = "是否消耗石头不能为空！")
    @Pattern(regexp = "TRUE|FALSE", message = "是否消耗石头只能为TRUE|FALSE")
    private String rockEnabled;
    private String boostingTask;
    private String boostingProgress;
    private String accountSituation;
    private String updateStatus;
}
