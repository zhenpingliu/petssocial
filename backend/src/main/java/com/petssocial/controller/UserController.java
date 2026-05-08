package com.petssocial.controller;

import com.petssocial.dto.UserProfileUpdateRequest;
import com.petssocial.entity.User;
import com.petssocial.security.SecurityUtils;
import com.petssocial.service.UserService;
import com.petssocial.vo.Result;
import com.petssocial.vo.UserInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户信息相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "获取当前用户信息")
    public Result<UserInfo> getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.getUserById(userId);
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(user, userInfo);
        return Result.success(userInfo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息")
    public Result<UserInfo> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(user, userInfo);
        return Result.success(userInfo);
    }

    @PutMapping("/profile")
    @Operation(summary = "更新用户信息")
    public Result<UserInfo> updateProfile(@RequestBody UserProfileUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.updateUserProfile(userId, request.getNickname(), 
                request.getGender(), request.getAvatar());
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(user, userInfo);
        return Result.success(userInfo);
    }
}
