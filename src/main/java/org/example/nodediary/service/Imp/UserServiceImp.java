package org.example.nodediary.service.Imp;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.nodediary.exception.BusinessException;
import org.example.nodediary.mapper.UserMapper;

import org.example.nodediary.pojo.CaptchaVO;
import org.example.nodediary.pojo.ChangePasswordDto;
import org.example.nodediary.pojo.RegisterDto;
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

    private static final String CAPTCHA_KEY_PREFIX = "captcha:register:";

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private HashEcode hashEcode;
    @Autowired
    StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();

    //生成图形验证码
    @Override
    public CaptchaVO generateCaptcha() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(130, 44, 4, 40);
        String code = captcha.getCode();
        String captchaId = IdUtil.simpleUUID();

        stringRedisTemplate.opsForValue().set(
                CAPTCHA_KEY_PREFIX + captchaId,
                code.toLowerCase(),
                2,
                TimeUnit.MINUTES
        );

        return new CaptchaVO(captchaId, captcha.getImageBase64Data());
    }

    //注册用户
    @Override
    public Integer register(RegisterDto dto) {
        //1.验证码校验
        if (dto.getCaptchaId() == null || dto.getCaptchaId().trim().isEmpty()) {
            throw new BusinessException("验证码不能为空");
        }
        if (dto.getCaptchaCode() == null || dto.getCaptchaCode().trim().isEmpty()) {
            throw new BusinessException("验证码不能为空");
        }
        String redisKey = CAPTCHA_KEY_PREFIX + dto.getCaptchaId();
        String realCode = stringRedisTemplate.opsForValue().get(redisKey);
        if (realCode == null) {
            throw new BusinessException("验证码已过期，请刷新后重试");
        }
        if (!realCode.equalsIgnoreCase(dto.getCaptchaCode().trim())) {
            throw new BusinessException("验证码错误");
        }
        //验证码一次性使用
        stringRedisTemplate.delete(redisKey);

        //2.基础字段校验
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        if (dto.getConfirmPassword() == null || dto.getConfirmPassword().trim().isEmpty()) {
            throw new BusinessException("请再次输入密码");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        if (dto.getUsername().length() < 3 || dto.getUsername().length() > 20) {
            throw new BusinessException("用户名长度应为 3-20 位");
        }
        if (dto.getPassword().length() < 6 || dto.getPassword().length() > 20) {
            throw new BusinessException("密码长度应为 6-20 位");
        }
        if (userMapper.selectByUsername(dto.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        //3.组装 User 并加密
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setNickname(dto.getNickname());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setGender(dto.getGender());
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

    //修改密码
    @Override
    public void changePassword(Integer userId, ChangePasswordDto dto) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (dto.getOldPassword() == null || dto.getOldPassword().trim().isEmpty()) {
            throw new BusinessException("原密码不能为空");
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().trim().isEmpty()) {
            throw new BusinessException("新密码不能为空");
        }
        if (dto.getConfirmPassword() == null || dto.getConfirmPassword().trim().isEmpty()) {
            throw new BusinessException("请再次输入新密码");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }
        if (dto.getNewPassword().length() < 6 || dto.getNewPassword().length() > 20) {
            throw new BusinessException("新密码长度应为 6-20 位");
        }
        if (dto.getNewPassword().equals(dto.getOldPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }

        User user = userMapper.selectByIdWithPassword(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!hashEcode.passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        //用新密码重新哈希
        User tmp = new User();
        tmp.setUsername(user.getUsername());
        tmp.setPassword(dto.getNewPassword());
        String newHash = hashEcode.hashPassword(tmp);

        userMapper.updatePassword(userId, newHash);

        //清理登录缓存，防止旧哈希残留
        stringRedisTemplate.delete("cache:user:" + user.getUsername());
    }
}
