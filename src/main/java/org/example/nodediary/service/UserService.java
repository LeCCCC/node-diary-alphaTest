package org.example.nodediary.service;

import org.example.nodediary.pojo.CaptchaVO;
import org.example.nodediary.pojo.ChangePasswordDto;
import org.example.nodediary.pojo.RegisterDto;
import org.example.nodediary.pojo.User;

public interface UserService {
    Integer register(RegisterDto dto);

    User login(User user);

    User getById(Integer userId);

    void updateUser(User user);

    CaptchaVO generateCaptcha();

    void changePassword(Integer userId, ChangePasswordDto dto);
}
