package com.miaosha.controller;

import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: Xinxin
 * @time: 2021/11/25 10:51
 */
public class BaseController {

    public static final String CONTENT_TYPE_FORMED = "application/x-www-form-urlencoded";

    /**
     * 对于web来说，controller层异常是最后一道关口，
     * 因此需要做好异常处理，
     * 否则仍会将异常抛到容器层
     */
    // 定义exceptionHandler解决未被controller层吸收的exception
    // 条件
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)  // 并非服务不能处理导致的错误，而是处理成功在业务逻辑上存在错误
    @ResponseBody
    public Object handlerEcxeption(HttpServletRequest request, Exception exception) {

        Map<String, Object> responseData = new HashMap<>();

        // 判断exception是否为BusinessException的子类对象
        if (exception instanceof BusinessException) {
            // 强转成BusinessException类型
            BusinessException businessException = (BusinessException) exception;

            responseData.put("errCode", businessException.getErrorCode());
            responseData.put("errMsg", businessException.getErrMsg());
        } else {
            exception.printStackTrace();
            responseData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrorCode());
            responseData.put("errMsg", EmBusinessError.UNKNOWN_ERROR.getErrMsg());
            responseData.put("ex", exception);
        }

        return CommonReturnType.create(responseData, "fail");
    }
}
