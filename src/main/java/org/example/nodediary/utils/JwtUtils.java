package org.example.nodediary.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    private static final String SIGN_KEY = "LecyYangLecyYangLecyYangLecyYang";
    private static final Long EXPIRE = 604800000L; // 7天

    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(SIGN_KEY.getBytes(StandardCharsets.UTF_8));

    //生成令牌
    public static String generateJwt(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .expiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(KEY)
                .compact();
    }

    //解析令牌
    public static Claims parseJWT(String jwt) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }
}