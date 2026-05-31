package com.example.journalApp;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
@SpringBootTest
class JournalApplicationTests {
	

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Test
	public void testRedis(){
		redisTemplate.opsForValue().set("email", "aryan@gmail.com");
		String value = (String) redisTemplate.opsForValue().get("email");
		assertEquals("aryan@gmail.com", value);
	}

	@Test
	void contextLoads() {
	}

}
