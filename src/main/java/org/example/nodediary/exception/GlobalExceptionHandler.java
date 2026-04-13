package org.example.nodediary.exception;

import org.example.nodediary.pojo.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    //捕捉全局异常
    @ExceptionHandler(Exception.class)
    public Result ex(Exception ex) {
        ex.printStackTrace();
        return Result.error("操作失败");
    }

    //捕捉业务异常
    @ExceptionHandler(BusinessException.class)
    public Result handleBusiness(BusinessException e) {
        e.printStackTrace();
        return new Result(e.getCode(), e.getMessage(), null);
    }
}
