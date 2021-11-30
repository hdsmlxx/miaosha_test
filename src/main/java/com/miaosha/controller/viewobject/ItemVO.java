package com.miaosha.controller.viewobject;

import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * @description:
 * @author: Xinxin
 * @time: 2021/11/26 10:39
 */
public class ItemVO {

    private Integer id;

    // 商品名称
    private String title;

    // 商品价格
    private BigDecimal price;

    // 商品库存
    private Integer stock;

    // 商品描述
    private String description;

    // 商品销量
    private Integer sales;

    // 商品图片url
    private String imgUrl;

    // 秒杀活动状态：0表示没有秒杀活动；1表示还未开始；2表示正在进行
    private Integer status;

    // 秒杀价格
    private BigDecimal promoPrice;

    // 秒杀活动Id
    private Integer promoId;

    // 秒杀活动开始时间
    private String startDate;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public BigDecimal getPromoPrice() {
        return promoPrice;
    }

    public void setPromoPrice(BigDecimal promoPrice) {
        this.promoPrice = promoPrice;
    }

    public Integer getPromoId() {
        return promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
