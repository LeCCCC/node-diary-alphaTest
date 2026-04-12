package org.example.nodediary.config;

import org.example.nodediary.interceptor.LoginCheckInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    //注入拦截器
    @Autowired
    private LoginCheckInterceptor loginCheckInterceptor;

    //对拦截器进行配置，拦截除了注册和登录以外的请求
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns("/**") // 拦截所有
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register"
                ); // 不拦截

    }
}
