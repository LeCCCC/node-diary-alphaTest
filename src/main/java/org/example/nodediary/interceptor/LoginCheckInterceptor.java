package org.example.nodediary.interceptor;

import com.alibaba.fastjson2.JSONObject;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.nodediary.pojo.BaseContext;
import org.example.nodediary.pojo.Result;
import org.example.nodediary.utils.JwtUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        // 获取 Authorization 请求头
        String authorization = req.getHeader("Authorization");

        // 判断请求头是否为空，或者格式不正确
        if (!StringUtils.hasLength(authorization) || !authorization.startsWith("Bearer ")) {
            Result error = Result.error("NOT_LOGIN");
            String notLogin = JSONObject.toJSONString(error);
            res.setStatus(401);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write(notLogin);
            return false;
        }

        // 截取真正的 token
        String jwt = authorization.substring(7);
        //如果令牌不为空，解析令牌
        try {
            Claims claims = JwtUtils.parseJWT(jwt);
            req.setAttribute("claims", claims);
            // 从 claims 中取出用户 id
            Object idObj = claims.get("id");
            Integer currentUserId = Integer.valueOf(idObj.toString());
            // 放入 ThreadLocal
            BaseContext.setCurrentId(currentUserId);
        } catch (Exception e) {
            e.printStackTrace();
            Result error = Result.error("NOT_LOGIN");
            String notLogin = JSONObject.toJSONString(error);
            res.setStatus(401);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write(notLogin);
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        // 移除 ThreadLocal 中的用户 id防止数据串行
        BaseContext.removeCurrentId();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
