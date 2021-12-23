package com.miaosha.service;

import com.miaosha.service.model.PromoModel;

public interface PromoService {
    PromoModel getPromoByItemId(Integer itemId);

    /**
     * 发布活动
     * @param id
     */
    void publishPromo(Integer id);

    /**
     * 生成活动令牌
     * @param promoId
     * @return
     */
    String generateSecondKillToken(Integer promoId, Integer itemId, Integer userId);
}
