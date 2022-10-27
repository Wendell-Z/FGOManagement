package com.fgo.management.exceptions;

import cn.hutool.core.util.StrUtil;
import com.fgo.management.dto.MyResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ResponseBody
@ControllerAdvice
public class MyExceptionHandler {


    @ExceptionHandler(value = Exception.class)
    public MyResponse handle(Exception e) {
        String message = e.getMessage();
        if (StrUtil.isBlankIfStr(message)) {
            message = e.getCause().getMessage();
        }
        return MyResponse.failed(message);
    }

}