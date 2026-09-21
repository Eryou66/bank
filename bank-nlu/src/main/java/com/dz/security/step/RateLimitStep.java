package com.dz.security.step;

import com.dz.api.ErrorCode;
import com.dz.exception.BizException;
import com.dz.ratelimit.GlobalTokenBucket;
import com.dz.ratelimit.RedisTokenBucket;
import com.dz.security.SecurityContext;
import com.dz.security.SecurityStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class RateLimitStep implements SecurityStep {

    private final RedisTokenBucket redisBucket;
    private final GlobalTokenBucket globalBucket;
    private final long userPerMinute;
    private final long ipPerMinute;
    private final long devicePerMinute;

    public RateLimitStep(RedisTokenBucket redisBucket,
                         GlobalTokenBucket globalBucket,
                         @Value("${bank.rate-limit.user-per-minute:20}") long userPerMinute,
                         @Value("${bank.rate-limit.ip-per-minute:100}") long ipPerMinute,
                         @Value("${bank.rate-limit.device-per-minute:50}") long devicePerMinute){
        this.redisBucket = redisBucket;
        this.globalBucket = globalBucket;
        this.userPerMinute = userPerMinute;
        this.ipPerMinute = ipPerMinute;
        this.devicePerMinute = devicePerMinute;
    }

    @Override
    public int order() {
        return 2;
    }

    @Override
    public String name() {
        return "四级并联频率限流";
    }

    @Override
    public ErrorCode errorCode() {
        return ErrorCode.RATE_LIMITED;
    }

    /**
     * 四级并联：任意一级触发即拦截，不再执行后续级别。
     * 全局级走本地 Guava（5000 QPS），用户 / IP / 设备三级走 Redis Lua 令牌桶。
     */
    @Override
    public void check(SecurityContext context) {
        if (!globalBucket.tryAcquire()) {
            log.warn("[限流] 全局级触发");
            throw new BizException(errorCode(), "系统繁忙，请稍后再试");
        }
        if (StringUtils.hasText(context.getUserId()) && !pass("user", context.getUserId(), userPerMinute)){
            log.warn("[限流] IP级触发 ip={}", context.getClientIp());
            throw new BizException(errorCode(), "请求过于频繁，请稍后再试");
        }
        if (StringUtils.hasText(context.getDeviceId()) && !pass("device", context.getDeviceId(), devicePerMinute)) {
            log.warn("[限流] 设备级触发 deviceId={}", context.getDeviceId());
            throw new BizException(errorCode(), "请求过于频繁，请稍后再试");
        }
    }

    private boolean pass(String dimension, String id, long perMinute){
        return redisBucket.tryAcquire("rate_limit:" + dimension + ":" + id, perMinute, perMinute / 60.0, 120);
    }

}
