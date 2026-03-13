package org.example.nodediary.utils;

import org.example.nodediary.pojo.User;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class HashEcode {

    //加密类
    public final PasswordEncoder passwordEncoder =
            Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();


    public String hashPassword(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        return encodedPassword;
    }
}
