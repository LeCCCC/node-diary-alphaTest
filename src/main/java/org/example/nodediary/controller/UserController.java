package org.example.nodediary.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.example.nodediary.pojo.*;

import org.example.nodediary.service.UserService;
;
import org.example.nodediary.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    //注册用户
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        if (user == null) {
            return Result.error("请求参数不能为空");
        }
        Integer userId = userService.register(user);
        Map<String, Integer> idClaim = new HashMap<>();
        idClaim.put("userId", userId);
        return Result.success("注册成功", idClaim);
    }

    //登录用户
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        if (user == null) {
            return Result.error("请求参数不能为空");
        }
        // 1. 登录校验，返回数据库中的用户信息
        User userFromDB = userService.login(user);
        if (userFromDB == null) {
            return Result.error("用户名或密码错误");
        }
        // 2. 生成 token
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userFromDB.getId());
        claims.put("username", userFromDB.getUsername());
        String token = JwtUtils.generateJwt(claims);
        // 3. 封装返回 data
        UserInfo userInfo = new UserInfo(
                userFromDB.getId(),
                userFromDB.getUsername(),
                userFromDB.getNickname(),
                userFromDB.getAvatarUrl()
        );
        LoginData data = new LoginData(token, "Bearer", userInfo);

        // 4. 返回结果
        return Result.success("登陆成功", data);
    }

    //根据 token 获取当前用户资料
    @GetMapping("/me")
    public Result getUserByToken(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null) {
            return Result.error("NOT_LOGIN");
        }
        Integer userId = Integer.valueOf(claims.get("id").toString());

        User user = userService.getById(userId);

        if (user == null) {
            return Result.error("用户不存在");
        }
        UserMe userMe = new UserMe(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getCreatedAt()
        );
        return Result.success("success", userMe);
    }
    //修改用户信息
    @PutMapping("/me")
    public Result updateUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            return Result.error("请求参数不能为空");
        }

        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null) {
            return Result.error("NOT_LOGIN");
        }

        Integer userId = Integer.valueOf(claims.get("id").toString());

        user.setId(userId);
        userService.updateUser(user);

        return Result.success("更新成功");
    }
    //退出登录
    @PostMapping("/logout")
    public Result userLogOut() {
        return Result.success("退出成功");
    }

}