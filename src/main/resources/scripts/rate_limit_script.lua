local key = KEYS[1]

local capacity = tonumber(ARGV[1])
local tokens_per_refill = tonumber(ARGV[2])
local refill_interval = tonumber(ARGV[3])

-- Validate configuration
if capacity == nil or capacity <= 0 then
    return redis.error_reply("capacity must be greater than 0")
end

if tokens_per_refill == nil or tokens_per_refill <= 0 then
    return redis.error_reply("tokens_per_refill must be greater than 0")
end

if refill_interval == nil or refill_interval <= 0 then
    return redis.error_reply("refill_interval must be greater than 0")
end


-- Get current Redis time and convert it to milliseconds
local redis_time = redis.call('TIME')

local now =
    tonumber(redis_time[1]) * 1000
    + tonumber(redis_time[2]) / 1000


-- Get current bucket state
local bucket = redis.call(
    'HMGET',
    key,
    'tokens',
    'last_refill'
)

local tokens = tonumber(bucket[1])
local last_refill = tonumber(bucket[2])


-- Initialize bucket on first request
if tokens == nil or last_refill == nil then
    tokens = capacity
    last_refill = now
end


-- Calculate how much time has passed since the last refill
local time_passed = math.max(
    0,
    now - last_refill
)


-- Calculate fractional tokens to add
local tokens_to_add =
    (time_passed / refill_interval)
    * tokens_per_refill


-- Refill bucket without exceeding capacity
tokens = math.min(
    capacity,
    tokens + tokens_to_add
)

last_refill = now


-- Try to consume one token
local allowed = 0

if tokens >= 1 then
    tokens = tokens - 1
    allowed = 1
end


-- Save updated bucket state
redis.call(
    'HSET',
    key,
    'tokens', tokens,
    'last_refill', last_refill
)


-- Calculate how long an empty bucket needs to become full.
-- Add one refill interval as a small safety buffer.
local ttl =
    math.ceil(
        (capacity / tokens_per_refill)
        * refill_interval
    ) + refill_interval


-- Delete bucket automatically after enough inactivity
redis.call(
    'PEXPIRE',
    key,
    ttl
)


-- Return:
-- [1] request allowed: 1 = yes, 0 = no
-- [2] remaining tokens
return {
    allowed,
    tostring(tokens)
}