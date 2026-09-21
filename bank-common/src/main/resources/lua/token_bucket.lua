-- 令牌桶限流（文档 2.1.2 第 2 步，原版基础上补了 EXPIRE 防 key 堆积）
-- KEYS[1]: 令牌桶 key
-- ARGV[1]: 桶容量   ARGV[2]: 每秒生成速率
-- ARGV[3]: 当前时间戳（秒）   ARGV[4]: key 过期秒数

local capacity = tonumber(ARGV[1])
local rate     = tonumber(ARGV[2])
local now      = tonumber(ARGV[3])
local ttl      = tonumber(ARGV[4])

local timeKey = KEYS[1] .. ':time'
local bucket = redis.call('get', KEYS[1])
local lastTime = redis.call('get', timeKey)

if not bucket or not lastTime then
    bucket = capacity
    lastTime = now
end

local interval  = now - tonumber(lastTime)
local newTokens = math.floor(interval * rate)
local tokens    = math.min(tonumber(bucket) + newTokens, capacity)

if tokens < 1 then
    redis.call('expire', KEYS[1], ttl)
    redis.call('expire', timeKey, ttl)
    return 0
end

redis.call('set', KEYS[1], tokens - 1)
redis.call('set', timeKey, now)
redis.call('expire', KEYS[1], ttl)
redis.call('expire', timeKey, ttl)
return 1