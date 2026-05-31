package com.example.journalApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig { //this is a configuration class for redis template
    
    @Bean
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) { //this is a bean for redis template
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());  //for key serializer to store string values 
        template.setValueSerializer(new StringRedisSerializer()); //for value serializer to store string values
		return template; //return the redis template
	}
}
