package org.example.nodediary.service.Imp;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.nodediary.annotation.ClearTreeHoleCache;
import org.example.nodediary.exception.BusinessException;
import org.example.nodediary.mapper.TreeHoleMapper;
import org.example.nodediary.pojo.BaseContext;
import org.example.nodediary.pojo.PageResult;
import org.example.nodediary.pojo.TreeHole;
import org.example.nodediary.pojo.TreeHoleVO;
import org.example.nodediary.service.TreeHoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TreeHoleServiceImp implements TreeHoleService {

    @Autowired
    private TreeHoleMapper treeHoleMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String TREE_HOLE_LIST_CACHE_KEY = "cache:tree_hole:list:";
    private static final String TREE_HOLE_LOCK_KEY = "lock:tree_hole:list:";
    private static final long CACHE_TTL = 5; // 缓存 5 分钟
    private static final long LOCK_TTL = 10; // 锁 10 秒

    @Override
    public PageResult<TreeHoleVO> getTreeHoleList(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;

        String cacheKey = TREE_HOLE_LIST_CACHE_KEY + pageNum + ":" + pageSize;
        String cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);

        // 命中缓存
        if (StrUtil.isNotBlank(cachedValue)) {
            if ("NULL".equals(cachedValue)) {
                return new PageResult<>(0L, List.of());
            }
            log.info("树洞列表缓存命中，pageNum={}, pageSize={}", pageNum, pageSize);
            return JSONUtil.toBean(cachedValue, PageResult.class);
        }

        // 缓存击穿：使用互斥锁
        String lockKey = TREE_HOLE_LOCK_KEY + pageNum + ":" + pageSize;
        String lockValue = String.valueOf(System.currentTimeMillis());

        try {
            // 尝试获取锁
            Boolean lockAcquired = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, LOCK_TTL, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(lockAcquired)) {
                // 获得锁，执行数据库查询
                log.info("获得锁，从数据库查询树洞列表，pageNum={}, pageSize={}", pageNum, pageSize);
                long offset = (long) (pageNum - 1) * pageSize;
                List<TreeHoleVO> records = treeHoleMapper.selectPage(offset, pageSize);
                Long total = treeHoleMapper.countAll();
                PageResult<TreeHoleVO> result = new PageResult<>(total, records);

                // 缓存结果
                String cacheValue = JSONUtil.toJsonStr(result);
                stringRedisTemplate.opsForValue().set(cacheKey, cacheValue, CACHE_TTL, TimeUnit.MINUTES);

                return result;
            } else {
                // 未获得锁，等待后重试（缓存击穿保护）
                log.info("未获得锁，等待后重试，pageNum={}, pageSize={}", pageNum, pageSize);
                Thread.sleep(50);

                // 重试查询缓存
                String retryCache = stringRedisTemplate.opsForValue().get(cacheKey);
                if (StrUtil.isNotBlank(retryCache)) {
                    if ("NULL".equals(retryCache)) {
                        return new PageResult<>(0L, List.of());
                    }
                    return JSONUtil.toBean(retryCache, PageResult.class);
                }

                // 缓存仍为空，直接查询数据库
                long offset = (long) (pageNum - 1) * pageSize;
                List<TreeHoleVO> records = treeHoleMapper.selectPage(offset, pageSize);
                Long total = treeHoleMapper.countAll();
                return new PageResult<>(total, records);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // 异常时降级处理，直接查询数据库
            long offset = (long) (pageNum - 1) * pageSize;
            List<TreeHoleVO> records = treeHoleMapper.selectPage(offset, pageSize);
            Long total = treeHoleMapper.countAll();
            return new PageResult<>(total, records);
        }
    }

    @Override
    @Transactional
    @ClearTreeHoleCache
    public Long publishTreeHole(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("树洞内容不能为空");
        }
        if (content.length() > 3000) {
            throw new BusinessException("树洞内容不能超过3000字");
        }
        Long userId = Long.valueOf(BaseContext.getCurrentId());
        TreeHole treeHole = new TreeHole(null, userId, content.trim());
        treeHoleMapper.insertTreeHole(treeHole);
        return treeHole.getTreeHoleId();
    }

    @Override
    public TreeHoleVO getTreeHoleDetail(Long treeHoleId) {
        TreeHoleVO treeHole = treeHoleMapper.selectById(treeHoleId);
        if (treeHole == null) {
            throw new BusinessException("树洞不存在", 404);
        }
        return treeHole;
    }

    @Override
    @Transactional
    @ClearTreeHoleCache
    public void updateTreeHole(Long treeHoleId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("树洞内容不能为空");
        }
        if (content.length() > 3000) {
            throw new BusinessException("树洞内容不能超过3000字");
        }
        TreeHoleVO treeHole = treeHoleMapper.selectById(treeHoleId);
        if (treeHole == null) {
            throw new BusinessException("树洞不存在", 404);
        }
        Long currentUserId = Long.valueOf(BaseContext.getCurrentId());
        if (!treeHole.getPublisherId().equals(currentUserId)) {
            throw new BusinessException("只能修改自己发布的树洞", 403);
        }
        treeHoleMapper.updateTreeHole(treeHoleId, content.trim());
    }

    @Override
    @Transactional
    @ClearTreeHoleCache
    public void deleteTreeHole(Long treeHoleId) {
        TreeHoleVO treeHole = treeHoleMapper.selectById(treeHoleId);
        if (treeHole == null) {
            throw new BusinessException("树洞不存在", 404);
        }
        Long currentUserId = Long.valueOf(BaseContext.getCurrentId());
        if (!treeHole.getPublisherId().equals(currentUserId)) {
            throw new BusinessException("只能删除自己发布的树洞", 403);
        }
        treeHoleMapper.deleteById(treeHoleId);
    }
}

