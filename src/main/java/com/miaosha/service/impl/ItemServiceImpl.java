package com.miaosha.service.impl;

import com.miaosha.dao.ItemDOMapper;
import com.miaosha.dao.ItemStockDOMapper;
import com.miaosha.dataobject.ItemDO;
import com.miaosha.dataobject.ItemStockDO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.service.ItemService;
import com.miaosha.service.PromoService;
import com.miaosha.service.model.ItemModel;
import com.miaosha.service.model.PromoModel;
import com.miaosha.validator.ValidationResult;
import com.miaosha.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: Xinxin
 * @time: 2021/11/26 9:49
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private PromoServiceImpl promoService;

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        // 校验入参
        if (itemModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        ValidationResult result = validator.validate(itemModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }

        // 转化itemModel -> dataObject
        ItemDO itemDO = convertItemDoFromModel(itemModel);

        // 写入数据库
        itemDOMapper.insertSelective(itemDO);

        itemModel.setId(itemDO.getId());

        ItemStockDO itemStockDO = convertItemStockDoFromModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);

        // 返回创建好的对象
        return this.getItemById(itemModel.getId());
    }

    private ItemDO convertItemDoFromModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }

        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel, itemDO);

        return itemDO;
    }

    private ItemStockDO convertItemStockDoFromModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOS = itemDOMapper.listItem();
        /**
         * 使用java8 stream，将itemDO map 成itemModel
         */
        List<ItemModel> itemModels = itemDOS.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertFromDataObject(itemDO, itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());

        return itemModels;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if (itemDO == null) {
            return null;
        }

        // 操作获得库存数量
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());

        ItemModel itemModel = convertFromDataObject(itemDO, itemStockDO);

        // 查询该商品的秒杀活动
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if (promoModel != null && promoModel.getStatus() != 3) {
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;
    }

    @Transactional
    @Override
    public boolean decreaceStock(Integer itemId, Integer amount) {
        int affectedRow = itemStockDOMapper.decreaceStock(amount, itemId);
        if (affectedRow > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    @Override
    public void increaseSales(Integer itemId, Integer amount) {
        itemDOMapper.increaseSales(itemId, amount);
    }

    private ItemModel convertFromDataObject(ItemDO itemDO, ItemStockDO itemStockDO) {
        if (itemDO == null || itemStockDO == null) {
            return null;
        }

        ItemModel itemModel = new ItemModel();

        BeanUtils.copyProperties(itemDO, itemModel);
        itemModel.setStock(itemStockDO.getStock());

        return itemModel;
    }
}
