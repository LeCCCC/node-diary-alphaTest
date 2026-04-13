package org.example.nodediary.service.Imp;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.nodediary.exception.BusinessException;
import org.example.nodediary.mapper.UserMapper;

import org.example.nodediary.pojo.PageResult;
import org.example.nodediary.pojo.User;

import org.example.nodediary.service.UserService;

import org.example.nodediary.utils.HashEcode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImp implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private HashEcode hashEcode;
    @Autowired
    StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
    //注册用户
    @Override
    public Integer register(User user) {
        //1.异常检测
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        } else if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        } else if (userMapper.selectByUsername(user.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        } else if (user.getUsername().length() < 3 || user.getUsername().length() > 20) {
            throw new BusinessException("用户名长度应为 3-20 位");
        } else if (user.getPassword().length() < 6 || user.getPassword().length() > 20) {
            throw new BusinessException("密码长度应为 6-20 位");
        }
        //用户名不存在，注册用户
        //对密码进行哈希加密
        user.setPassword(hashEcode.hashPassword(user));
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insertUser(user);
        return user.getId();
    }

    //用户登录
    @Override
    public User login(User user) {
        // 1. 参数校验
        String username = user.getUsername();
        String password = user.getPassword();

        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }

        username = username.trim();

        // 2. 查询缓存
        String redisKey = "cache:user:" + username;
        String userJson = stringRedisTemplate.opsForValue().get(redisKey);

        User loginUser = null;

        // 2.1 命中空值缓存：说明用户不存在
        if ("NULL".equals(userJson)) {
            throw new BusinessException("用户名或密码错误");
        }

        // 2.2 命中正常缓存
        if (StrUtil.isNotBlank(userJson)) {
            loginUser = JSONUtil.toBean(userJson, User.class);
            log.info("登录命中 Redis 缓存，username={}", username);
        } else {
            // 2.3 未命中缓存，查数据库
            loginUser = userMapper.selectByUsername(username);

            if (loginUser == null) {
                // 防缓存穿透：缓存空值，TTL 要短
                stringRedisTemplate.opsForValue().set(redisKey, "NULL", 60, TimeUnit.SECONDS);
                throw new BusinessException("用户名或密码错误");
            }

            // 3. 写入缓存
            String cacheValue = JSONUtil.toJsonStr(loginUser);
            stringRedisTemplate.opsForValue().set(redisKey, cacheValue, 30, TimeUnit.MINUTES);
            log.info("登录查询数据库并写入 Redis，username={}", username);
        }

        // 4. 校验密码
        if (!hashEcode.passwordEncoder.matches(password, loginUser.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 5. 返回脱敏后的用户信息
        loginUser.setPassword(null);
        return loginUser;
    }

    //根据id查询
    @Override
    public User getById(Integer userId) {
        return userMapper.selectById(userId);
    }
    //更新用户信息
    @Override
    public void updateUser(User user) {
        if (user.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }

        User oldUser = userMapper.selectById(user.getId());
        if (oldUser == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getNickname() != null && user.getNickname().length() > 20) {
            throw new BusinessException("昵称长度不能超过20");
        }

        userMapper.updateUser(user);
    }
}
