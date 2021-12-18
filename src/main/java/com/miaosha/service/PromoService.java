package com.miaosha.service;

import com.miaosha.service.model.PromoModel;

public interface PromoService {
    PromoModel getPromoByItemId(Integer itemId);

    /**
     * 发布活动
     * @param id
     */
    void publishPromo(Integer id);
}
