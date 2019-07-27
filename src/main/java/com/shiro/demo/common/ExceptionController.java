package com.shiro.demo.common;

import com.shiro.demo.exception.MyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * SpringMVC异常处理
 *
 * @author zhankguan
 */
@RestControllerAdvice
public class ExceptionController {

    /**
     * 处理系统异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(Exception.class)
    public JsonResult handlerException(Exception ex) {
        ex.printStackTrace();
        return JsonResult.error("系统异常", ex.getMessage());
    }

    /**
     * 处理自定义异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(MyException.class)
    public JsonResult handlerMyException(MyException ex) {
        ex.printStackTrace();
        return JsonResult.error("自定义异常", ex.getMessage());
    }

}
