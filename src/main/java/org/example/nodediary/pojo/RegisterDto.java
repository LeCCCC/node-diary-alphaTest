package org.example.nodediary.pojo;

import lombok.Data;

@Data
public class RegisterDto {
    private String username;
    private String password;
    private String confirmPassword;
    private String nickname;
    private String avatarUrl;
    private Short gender;
    private String captchaId;
    private String captchaCode;
}
