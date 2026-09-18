package com.yking.mallcommon.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{
    private  final int code;

    /**
     * 使用默认业务状态码400，业务异常
     * @param message
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    /**
     * 使用自定义业务状态码，业务异常
     * @param code
     * @param message
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
