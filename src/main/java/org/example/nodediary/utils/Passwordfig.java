package org.example.nodediary.utils;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
//密码加密相关配置
public class Passwordfig {
    public static PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(
                16,   // saltLength
                32,   // hashLength
                1,    // parallelism
                1 << 14, // memory，单位 KB，这里是 16384 KB = 16 MB
                2     // iterations
        );
    }
}
