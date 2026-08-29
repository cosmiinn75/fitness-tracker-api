package com.cosmin.fitness_tracker_api.security.rateLimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;

@Service
@SuppressWarnings("rawtypes")
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List> tokenBucketScript;

    public RateLimitService(StringRedisTemplate redisTemplate, RedisScript<List> tokenBucketScript) {
        this.redisTemplate = redisTemplate;
        this.tokenBucketScript = tokenBucketScript;
    }

    public RateLimitAnswer tryConsume(String key,
                              RateLimitPolicy rateLimitPolicy){
        List<?> result = redisTemplate.execute(
                tokenBucketScript,
                List.of(key),
                String.valueOf(rateLimitPolicy.capacity()),
                String.valueOf(rateLimitPolicy.tokensPerRefill()),
                String.valueOf(rateLimitPolicy.refillIntervalsMs())
        );

        if(result == null || result.size() < 2){
            throw new IllegalStateException("Invalid response from rate limit script");
        }

        boolean allowed = ((Number) result.getFirst()).longValue() == 1;
        double remainingTokens = Double.parseDouble(result.get(1).toString());
        
        return new RateLimitAnswer(
                allowed,remainingTokens
        );
    }


}
