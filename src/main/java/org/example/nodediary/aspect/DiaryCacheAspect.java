package org.example.nodediary.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.nodediary.mapper.MatchMapper;
import org.example.nodediary.pojo.BaseContext;
import org.example.nodediary.service.MatchService;
import org.example.nodediary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
@Slf4j
@Aspect
@Component
public class DiaryCacheAspect {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MatchMapper matchMapper;

    @AfterReturning("@annotation(org.example.nodediary.annotation.ClearDiaryCache)")
    public void afterReturning() {
        evictRelatedCaches();
    }

    private void evictRelatedCaches() {
        Integer currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            return;
        }

        // 清理当前用户的首页流缓存
        String currentUserCacheKey = "cache:home_feed:" + currentUserId;
        stringRedisTemplate.delete(currentUserCacheKey);
        stringRedisTemplate.delete("cache:diary_list:" + currentUserId);
        log.info("已清理当前用户({})的首页流缓存和日记列表缓存", currentUserId);

        // 查找当前用户的匹配对象ID
        Integer matchedUserId = matchMapper.selectMatchUserId(currentUserId);
        if (matchedUserId != null) {
            // 清理匹配对象的首页流缓存
            String matchedUserCacheKey = "cache:home_feed:" + matchedUserId;
            stringRedisTemplate.delete(matchedUserCacheKey);
            log.info("已清理匹配对象({})的首页流缓存", matchedUserId);
        }
    }
}
