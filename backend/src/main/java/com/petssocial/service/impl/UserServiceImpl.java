package com.petssocial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petssocial.entity.User;
import com.petssocial.mapper.UserMapper;
import com.petssocial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getUserByPhone(String phone) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone));
    }

    @Override
    public User updateUserProfile(Long userId, String nickname, Integer gender, String avatar) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (nickname != null) user.setNickname(nickname);
        if (gender != null) user.setGender(gender);
        if (avatar != null) user.setAvatar(avatar);
        userMapper.updateById(user);
        return user;
    }
}
