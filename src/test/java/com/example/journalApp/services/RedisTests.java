package com.example.journalApp.services;

import com.example.journalApp.JournalApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = JournalApplication.class)
public class RedisTests {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void testRedis(){
        redisTemplate.opsForValue().set("email", "aryan@gmail.com");
        String value = (String) redisTemplate.opsForValue().get("email");
        String name = (String) redisTemplate.opsForValue().get("name");
        assertEquals("aryan@gmail.com", value);
        assertEquals("aryan",name);
    }
}
