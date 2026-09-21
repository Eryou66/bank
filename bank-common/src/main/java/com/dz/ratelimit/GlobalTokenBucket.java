package com.dz.ratelimit;


import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 全局级限流：本地 Guava 令牌桶。
 * 文档 2.1.2 特意解释了为什么这级不走 Redis —— 5000 QPS 下每次请求都访问 Redis 会把 Redis 打崩，
 * 本地令牌桶只依赖 JVM 内存，延迟在微秒级。
 */
@Component
public class GlobalTokenBucket {

    private final RateLimiter limiter;

    public GlobalTokenBucket(@Value("${bank.rate-limit.global-qps:5000}")double qps) {
        this.limiter = RateLimiter.create(qps);
    }

    public boolean tryAcquire(){
        return limiter.tryAcquire();
    }
}
