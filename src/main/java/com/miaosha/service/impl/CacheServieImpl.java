package com.miaosha.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.miaosha.service.CacheService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Service
public class CacheServieImpl implements CacheService {

    private Cache<String, Object> commonCache = null;

    @PostConstruct
    public void init() {
        commonCache = CacheBuilder.newBuilder()
                //设置缓存的初始容量
                .initialCapacity(10)
                //设置缓存最大容量，超过最大值lru算法移除
                .maximumSize(100)
                //设置写缓存后多少秒过期
                .expireAfterWrite(60, TimeUnit.SECONDS).build();

    }

    @Override
    public void setCommonCache(String key, Object value) {
        commonCache.put(key, value);
    }

    @Override
    public Object getFromCommonCache(String key) {
        return commonCache.getIfPresent(key);
    }
}
