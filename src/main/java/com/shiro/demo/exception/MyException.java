package com.shiro.demo.exception;

/**
 * 自定义异常
 *
 * @author zhangkuan
 */
public class MyException extends Exception {

    private Integer code;

    private String message;

    public MyException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
