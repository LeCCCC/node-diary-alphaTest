package org.example.nodediary.service;

import org.example.nodediary.pojo.User;

public interface UserService {
    Integer register(User user);

    User login(User user);

    User getById(Integer userId);

    void updateUser(User user);
}
