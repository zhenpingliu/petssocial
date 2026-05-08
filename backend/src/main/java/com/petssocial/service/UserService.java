package com.petssocial.service;

import com.petssocial.entity.User;

public interface UserService {
    User getUserById(Long id);
    User getUserByPhone(String phone);
    User updateUserProfile(Long userId, String nickname, Integer gender, String avatar);
}
