package com.miaosha.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.response.CommonReturnType;
import com.miaosha.service.OrderService;
import com.miaosha.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller("order")
@RequestMapping("/order")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class OrderController extends BaseController {

    @Autowired
    OrderService orderService;

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    // 封装下单请求
    @RequestMapping(value = "/createOrder", method = RequestMethod.POST, consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "amount") Integer amount) throws BusinessException {
        // 判断用户是否登录
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL, "用户未登录");
        }

        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        /*Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if (isLogin == null || !isLogin.booleanValue()) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL, "用户未登录");
        }
        // 执行相应的service
        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");*/

        orderService.createOrder(userModel.getId(), itemId, amount);

        return CommonReturnType.create(null);
    }
}
