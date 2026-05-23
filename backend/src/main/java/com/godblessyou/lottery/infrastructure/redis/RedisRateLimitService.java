package com.godblessyou.lottery.infrastructure.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisRateLimitService {

    private final StringRedisTemplate stringRedisTemplate;

    public boolean tryAcquire(String key, long limit, Duration window) {
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, window);
        }
        return count != null && count <= limit;
    }
}
