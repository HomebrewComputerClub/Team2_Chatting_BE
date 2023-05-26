package com.chatting.homebrewchat.jwt.util;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate stringRedisTemplate;

    public String getData(String key){
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String data = valueOperations.get(key);
        if (data == null) {
            throw new RuntimeException("Failed to retrieve data from Redis.");
        }
        return data;
    }

    public void setData(String key, String value){
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.set(key, value);
    }

    public void setDataExpire(String key, String value, long duration){
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(key ,value, expireDuration);
    }

    public void deleteData(String key){
//        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
//        valueOperations.getAndDelete()
        stringRedisTemplate.delete(key);
    }
}
