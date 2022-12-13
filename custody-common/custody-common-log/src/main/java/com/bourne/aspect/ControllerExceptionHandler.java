package com.bourne.aspect;

import com.bourne.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * @Author: vhtk
 * @Description:
 * @Date: 2020/6/22
 */
@ControllerAdvice
public class ControllerExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handleException(Throwable e, HttpServletRequest req) {
        logger.error("Catch business exception: {}", e.getMessage(), e);
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException bind = (MethodArgumentNotValidException) e;
            List<ObjectError> allErrors = bind.getBindingResult().getAllErrors();
            StringBuffer message = new StringBuffer();
            allErrors.forEach(error -> message.append(error.getDefaultMessage()).append(";"));
            return Result.fail("请求参数错误" + req.getRequestURL().toString(), message.toString());
        } else if (e instanceof BusinessException) {
            BusinessException businessException = (BusinessException) e;
            return Result.info(businessException.getCode(), "请求发生异常" + req.getRequestURL().toString(), businessException.getMessage());
        } else {
            return Result.fail("请求发生异常" + req.getRequestURL().toString(), e.getMessage());
        }
    }
}

