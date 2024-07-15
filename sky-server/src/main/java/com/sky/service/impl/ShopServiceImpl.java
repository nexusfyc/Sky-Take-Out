package com.sky.service.impl;

import com.sky.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShopServiceImpl implements ShopService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void setStatus(Integer status) {
        redisTemplate.opsForValue().set("shop_status", status);
    }

    @Override
    public Integer getStatus() {
        return (Integer) redisTemplate.opsForValue().get("shop_status");
    }
}
