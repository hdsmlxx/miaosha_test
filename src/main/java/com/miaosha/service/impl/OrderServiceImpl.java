package com.miaosha.service.impl;

import com.miaosha.dao.OrderDOMapper;
import com.miaosha.dao.SequenceDOMapper;
import com.miaosha.dataobject.OrderDO;
import com.miaosha.dataobject.SequenceDO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.mq.MqProducer;
import com.miaosha.service.ItemService;
import com.miaosha.service.OrderService;
import com.miaosha.service.UserService;
import com.miaosha.service.model.ItemModel;
import com.miaosha.service.model.OrderModel;
import com.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private MqProducer producer;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer promoId, Integer itemId, Integer amount) throws BusinessException {
        // 校验下单状态：商品是否存在，用户是否合法，购买数量是否正确
//        ItemModel itemModel = itemService.getItemById(itemId);

        // 从缓存中获取 itemModel
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if (itemModel == null) {
            throw new BusinessException(EmBusinessError.ITEM_NOT_EXIST);
        }

//        UserModel userModel = userService.getUserById(userId);
        UserModel userModel = userService.getUserByIdInCache(userId);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }

        if (amount <= 0 || amount > 99) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "购买数量信息不正确");
        }

        //校验活动信息
        if (promoId != null && itemModel.getPromoModel() != null) {
            //(1)校验对应活动是否存在这个适用商品
            if (promoId.intValue() != itemModel.getPromoModel().getId()) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息不存在");
            } else if (itemModel.getPromoModel().getStatus().intValue() != 2) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动状态不对");
            }
        }

        // 落单前减库存
        boolean result = itemService.decreaceStock(itemId, amount);
        if (!result) {
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        // 落单
        OrderModel orderModel = new OrderModel();
        orderModel.setId(generateOrderNo());
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        if (promoId != null && itemModel.getPromoModel() != null) {
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoPrice());
        } else {
            orderModel.setItemPrice(itemModel.getPrice());
        }

        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(BigDecimal.valueOf(amount)));

        OrderDO orderDO = convertFromModel(orderModel);
        orderDOMapper.insertSelective(orderDO);

        // 增加商品销量
        itemService.increaseSales(itemId, amount);

        // 返回前端

        return orderModel;
    }

    // 无论外部的事务执行成功与否，本代码块执行后包含的事务都会被提交
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateOrderNo() {
        // 共16位
        StringBuilder builder = new StringBuilder();
        // 前8位为时间信息
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        builder.append(nowDate);
        // 中间6位为自增序列
        // 获取当前sequence
        int sequence = 0;
        // 问题1：当事务失败，发生回滚，下次事务取到的值仍然是上次事务的失败值，应该杜绝这种违反全局唯一性的取值
        // 解决办法：使用注解属性
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() + sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String sequenceStr = String.valueOf(sequence);

        // 问题2：当sequence跳增到超过6位时，会出现“溢出”问题
        // 解决办法：在sequence_info表中增加最大值和初始值字段，
        // 当待跳增的值大于最大值时，恢复初始值
        for (int i = 0; i < 6 - sequenceStr.length(); i ++) {
            builder.append("0");
        }
        builder.append(sequenceStr);

        // 后2位为分库分表位
        // 不考虑分库分表
        builder.append("00");

        return builder.toString();
    }

    private OrderDO convertFromModel(OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        }

        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel, orderDO);
        return orderDO;
    }
}
