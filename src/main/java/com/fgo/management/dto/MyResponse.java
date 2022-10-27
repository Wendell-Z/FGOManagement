package com.fgo.management.dto;

import com.fgo.management.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyResponse {

    private int code;

    private String message;

    private Object data;


    public static MyResponse success(Object data) {
        return new MyResponse(200, Constants.EMPTY_STRING, data);
    }

    public static MyResponse failed(String message) {
        return new MyResponse(500, message, null);
    }

    public static MyResponse success() {
        return MyResponse.success(null);
    }

    public static MyResponse success(String message) {
        return new MyResponse(200, message, null);
    }
}
