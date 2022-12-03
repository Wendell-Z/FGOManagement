package com.fgo.management.exceptions;

import cn.hutool.core.util.StrUtil;
import com.fgo.management.dto.MyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.function.Function;
import java.util.stream.Collectors;

@ResponseBody
@ControllerAdvice
public class MyExceptionHandler {
    public static final Logger LOGGER = LoggerFactory.getLogger(MyExceptionHandler.class);


    @ExceptionHandler(value = Exception.class)
    public MyResponse handle(Exception e) {
        String message;
        if (e instanceof MethodArgumentNotValidException) {
            message = ((MethodArgumentNotValidException) e)
                    .getAllErrors()
                    .stream()
                    .map((Function<ObjectError, Object>) DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList())
                    .toString();
        } else {
            message = e.getMessage();
        }
        if (StrUtil.isBlankIfStr(message)) {
            message = e.getCause().getMessage();
        }
        LOGGER.error("统一异常捕捉:{}", message);
        e.printStackTrace();
        return MyResponse.failed(message);
    }

}