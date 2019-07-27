package com.shiro.demo.common;

import lombok.Data;

import java.io.Serializable;

/**
 * JsonResult
 *
 * @author zhangkuan
 */
@Data
public class JsonResult implements Serializable {

    private Integer code;

    private String message;

    private Object data;

    public static JsonResult ok() {
        return JsonResult.ok("操作成功", "");
    }

    public static JsonResult ok(String message) {
        return JsonResult.ok(message, "");
    }

    public static JsonResult ok(Object data) {
        return JsonResult.ok("操作成功", data);
    }

    public static JsonResult ok(String message, Object data) {
        return new JsonResult(0, message, data);
    }

    public static JsonResult error() {
        return JsonResult.error("操作失败", "");
    }

    public static JsonResult error(String message) {
        return JsonResult.error(message, "");
    }

    public static JsonResult error(Object data) {
        return JsonResult.error("操作失败", data);
    }

    public static JsonResult error(String message, Object data) {
        return new JsonResult(-1, message, data);
    }

    private static JsonResult error(int code, String message, Object data) {
        return new JsonResult(code, message, data);
    }

    private JsonResult(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    @Override
    public String toString() {
        return "{ \"code\"=" + code +
                ", \"message\"=\"" + message +
                "\", \"data\"=\"" + data + "\"}";
    }
}
