package org.example.nodediary.service.Imp;

import org.example.nodediary.exception.BusinessException;
import org.example.nodediary.mapper.UserMapper;

import org.example.nodediary.pojo.User;

import org.example.nodediary.service.UserService;

import org.example.nodediary.utils.HashEcode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImp implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private HashEcode hashEcode;

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
        //1.异常检测
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        } else if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        //2.查询用户
        User userFromDB = userMapper.selectByUsername(user.getUsername());
        if (userFromDB == null) {
            throw new BusinessException("用户名不存在");
        }
        //3.验证密码
        if (!hashEcode.passwordEncoder.matches(user.getPassword(), userFromDB.getPassword())) {
            throw new BusinessException("密码错误");
        }
        //4.返回用户信息
        return userFromDB;
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

        // 可按需加校验
        if (user.getNickname() != null && user.getNickname().length() > 20) {
            throw new BusinessException("昵称长度不能超过20");
        }

        userMapper.updateUser(user);
    }
}
