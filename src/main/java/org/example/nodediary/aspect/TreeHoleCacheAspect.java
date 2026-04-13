package org.example.nodediary.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Aspect
@Component
public class TreeHoleCacheAspect {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String TREE_HOLE_LIST_CACHE_KEY_PREFIX = "cache:tree_hole:list:*";
    private static final String TREE_HOLE_LOCK_KEY_PREFIX = "lock:tree_hole:list:*";

    @After("@annotation(org.example.nodediary.annotation.ClearTreeHoleCache)")
    public void clearTreeHoleCache() {
        try {
            // 删除所有树洞列表缓存键
            Set<String> cacheKeys = stringRedisTemplate.keys(TREE_HOLE_LIST_CACHE_KEY_PREFIX);
            if (cacheKeys != null && !cacheKeys.isEmpty()) {
                stringRedisTemplate.delete(cacheKeys);
                log.info("清除树洞列表缓存，共 {} 个键", cacheKeys.size());
            }

            // 删除所有树洞锁键
            Set<String> lockKeys = stringRedisTemplate.keys(TREE_HOLE_LOCK_KEY_PREFIX);
            if (lockKeys != null && !lockKeys.isEmpty()) {
                stringRedisTemplate.delete(lockKeys);
                log.info("清除树洞锁，共 {} 个键", lockKeys.size());
            }
        } catch (Exception e) {
            log.error("清除树洞缓存失败", e);
        }
    }
}
