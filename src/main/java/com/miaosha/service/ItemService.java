package com.miaosha.service;

import com.miaosha.error.BusinessException;
import com.miaosha.service.model.ItemModel;

import java.util.List;

public interface ItemService {

    // 创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    // 商品列表浏览
    List<ItemModel> listItem();

    // 商品详情浏览
    ItemModel getItemById(Integer id);

    /**
     * item 及 promo model 缓存模型
     * @param id
     * @return
     */
    ItemModel getItemByIdInCache(Integer id);

    /**
     * 初始化库存流水
     * @param itemId
     * @param amount
     */
    String initStockLog(Integer itemId, Integer amount);

    /**
     * 库存扣减
     * @param itemId
     * @param amount
     * @return
     */
    boolean decreaceStock(Integer itemId, Integer amount);

    /**
     * 回补库存
     * @param itemId
     * @param amount
     * @return
     */
    boolean increaseStock(Integer itemId, Integer amount);

    /**
     * 异步更新库存
     * @param itemId
     * @param amount
     * @return
     */
    boolean asyndecreaseStock(Integer itemId, Integer amount);

    /**
     * 增加销量
     * @param itemId
     * @param amount
     */
    void increaseSales(Integer itemId, Integer amount);

}
