package com.fgo.management.mapper;

import com.fgo.management.dto.OrderBoostingInfo;
import com.fgo.management.dto.OrderStatusInfo;
import com.fgo.management.dto.QueryOrderCondition;
import com.fgo.management.enums.OrderStatus;
import com.fgo.management.model.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    void insert(@Param("order") OrderDetail orderDetail);

    void update(@Param("order") OrderDetail orderDetail);

    List<OrderDetail> queryOrderDetails(@Param("condition") QueryOrderCondition condition);

    void updateOrderStatus(@Param("statusInfo") OrderStatusInfo orderStatusInfo);

    OrderDetail queryByOrderId(@Param("orderId") long orderId);

    List<OrderDetail> queryByPlayerAccountWithLock(@Param("account") String playerAccount);

    void setOrderBoostingTask(@Param("info") OrderBoostingInfo orderBoostingInfo);

    void updateOrderSituationById(@Param("id") long id, @Param("json") String beanJson);

    String queryBoostingProgressByOrderId(@Param("orderId") long orderId);


    void batchUpdateOrderStatus(@Param("list") List<String> orderIds, @Param("status") OrderStatus orderStatus);

    List<OrderDetail> queryByUpdateStatus(@Param("updateStatus") String updateStatus);

    void updateProgress(@Param("bean") OrderDetail orderDetail);

    int updateToSyncFollower(@Param("id") long orderId);

    String queryFollowerInfoByOrderId(@Param("id") long orderId);
}
