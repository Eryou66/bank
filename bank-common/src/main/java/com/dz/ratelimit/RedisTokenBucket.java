package com.dz.ratelimit;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class RedisTokenBucket {

    private static final DefaultRedisScript<Long> SCRIPT = new DefaultRedisScript<>();

    static{
        SCRIPT.setLocation(new ClassPathResource("lua/token_bucket.lua"));
        SCRIPT.setResultType(Long.class);
    }

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public boolean tryAcquire(String key, long capacity, double ratePerSecond, long expireSeconds){
        long now = System.currentTimeMillis() / 1000;
        Long result = stringRedisTemplate.execute(
                SCRIPT,
                Collections.singletonList(key),
                String.valueOf(capacity),
                String.valueOf(ratePerSecond),
                String.valueOf(now),
                String.valueOf(expireSeconds)
        );
        return result != null && result == 1L;

    }

}
