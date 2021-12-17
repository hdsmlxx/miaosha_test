package com.miaosha.service;

import com.miaosha.error.BusinessException;
import com.miaosha.service.model.OrderModel;

public interface OrderService {

    /**
     * 1.通过前端url传过来的秒杀活动id，在下单接口内校验id是否属于对应商品切活动一开始
     * 2.直接在下单接口内判断对应商品是否存在秒杀活动，若存在且在进行中则以秒杀价格下单
     * 方法1更好
     * @param userId
     * @param promoId
     * @param itemId
     * @param amount
     * @return
     * @throws BusinessException
     */
    OrderModel createOrder(Integer userId, Integer promoId, Integer itemId, Integer amount) throws BusinessException;
}
