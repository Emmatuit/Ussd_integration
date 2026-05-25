package com.interswitch.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;
import java.util.Map;

@Configuration
public class RedisConfig {

    @SuppressWarnings("deprecation")
	@Bean
    public RedisTemplate<String, List<Map<String, Object>>> bankListRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, List<Map<String, Object>>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key serializer
        template.setKeySerializer(new StringRedisSerializer());

        // Value serializer — non-deprecated, uses JavaType
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        JavaType javaType = typeFactory.constructCollectionType(
                List.class,
                typeFactory.constructMapType(Map.class, String.class, Object.class)
        );

        Jackson2JsonRedisSerializer<List<Map<String, Object>>> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, javaType);

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}