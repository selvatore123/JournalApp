package com.example.journalApp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper mapper; //for converting the value to the EntityClass

    public <T> T get(String Key, Class<T> EntityClass){
        try {
            Object value = redisTemplate.opsForValue().get(Key);
            if(value == null){
                log.error("Value is null for key: {}", Key);
                return null;
            }
            return mapper.readValue(value.toString(), EntityClass); //convert the value to the EntityClass 
        } catch (Exception e) {
            log.error("Error converting value to EntityClass", e); //log the error
            throw new RuntimeException("Error converting value to EntityClass", e); //throw an exception
        }
    }

    public void set(String Key, Object Value, long ttl) {
        if (Key == null || Value == null) {
            log.error("Value or key is null");
            throw new RuntimeException("Value or key is null");
        }
        try {
            String jsonValue = mapper.writeValueAsString(Value);
            redisTemplate.opsForValue().set(Key, jsonValue, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error setting value for key: {}", Key, e);
            throw new RuntimeException("Error setting value for key: " + Key, e);
        }
    }
}
