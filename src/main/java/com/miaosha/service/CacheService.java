package com.miaosha.service;

public interface CacheService {

    //存方法
    void setCommonCache(String key, Object value);

    //取方法
    Object getFromCommonCache(String key);
}
