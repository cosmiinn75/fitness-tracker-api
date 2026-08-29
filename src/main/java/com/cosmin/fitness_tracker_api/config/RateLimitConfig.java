package com.cosmin.fitness_tracker_api.config;

import com.cosmin.fitness_tracker_api.security.rateLimit.RateLimitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;


@Configuration
@SuppressWarnings("rawtypes")
public class RateLimitConfig {

    @Bean
    public RedisScript<List> tokenBucketScript(){
        return RedisScript.of(new ClassPathResource("scripts/rate_limit_script.lua"),List.class);
    }



}
