package com.fgo.management.aspects;

import cn.hutool.json.JSONUtil;
import com.fgo.management.enums.ActivePower;
import com.fgo.management.model.OrderDetail;
import com.fgo.management.model.ParamConfig;
import com.fgo.management.service.OrderDetailService;
import com.fgo.management.service.ParamConfigService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Locale;

@Component
@Aspect
public class OrderDetailToJsonAspect {


    private final static String ID = "id";

    private final static String ORDER_ID = "orderId";

    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ParamConfigService paramConfigService;


    @Pointcut("@annotation(com.fgo.management.annotations.OrderDetailToJson)")
    public void pointcut() {

    }

    @Around("@annotation(com.fgo.management.annotations.OrderDetailToJson)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object obj = joinPoint.proceed();
        Object arg = joinPoint.getArgs()[0];
        Field[] declaredFields = arg.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getName().equals(ID) || field.getName().equals(ORDER_ID)) {
                field.setAccessible(true);
                Long id = (Long) field.get(arg);
                OrderDetail orderDetail = orderDetailService.queryOrderDetailById(id);
                ParamConfig paramConfig = paramConfigService.queryByParam("EVENT", "ACTIVE_POWER");
                String[] enums = paramConfig.getParamValue().split(",");
                for (String anEnum : enums) {
                    ActivePower activePower = ActivePower.valueOf(anEnum);
                    orderDetail.setFruitEnabled(Boolean.valueOf(activePower == ActivePower.FRUIT).toString().toUpperCase(Locale.ROOT));
                    orderDetail.setRockEnabled(Boolean.valueOf(activePower == ActivePower.ROCK).toString().toUpperCase(Locale.ROOT));
                }
                orderDetailService.updateOrderSituationById(id, JSONUtil.parse(orderDetail).toString());
            }
        }
        return obj;
    }
}
