package org.example.nodediary.service;

import org.example.nodediary.pojo.*;

import java.util.List;

public interface UserService {
    Integer register(RegisterDto dto);

    User login(User user);

    User getById(Integer userId);

    void updateUser(User user);

    CaptchaVO generateCaptcha();

    void changePassword(Integer userId, ChangePasswordDto dto);

    List<Tag> getAllTags();

    List<Integer> getUserTagIds(Integer userId);

    void saveUserTags(Integer userId, List<Integer> tagIds);
}
