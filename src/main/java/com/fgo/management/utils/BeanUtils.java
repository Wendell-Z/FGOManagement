package com.fgo.management.utils;

import java.lang.reflect.Field;

public class BeanUtils {

    private BeanUtils() {
    }

    public static void setNullField(Object bean, String nullValue) {
        try {
            Class<?> beanClass = bean.getClass();
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                if (fieldType == String.class) {
                    Object fieldValue = field.get(bean);
                    if (fieldValue == null) {
                        field.set(bean, nullValue);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void trimStringField(Object bean) {
        try {
            Class<?> beanClass = bean.getClass();
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Class<?> fieldDeclaringClass = field.getType();
                if (fieldDeclaringClass == String.class) {
                    String fieldValue = (String) field.get(bean);
                    if (fieldValue != null) {
                        field.set(bean, fieldValue.trim());
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
