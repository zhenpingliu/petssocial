package com.petssocial.controller;

import com.petssocial.dto.LoginRequest;
import com.petssocial.dto.RefreshTokenRequest;
import com.petssocial.dto.RegisterRequest;
import com.petssocial.service.AuthService;
import com.petssocial.vo.AuthResponse;
import com.petssocial.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "手机号验证码登录")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token")
    public Result<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.success(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/sms/send")
    @Operation(summary = "发送验证码")
    public Result<Void> sendSms(@RequestParam String phone, @RequestParam(defaultValue = "1") Integer type) {
        authService.sendVerificationCode(phone, type);
        return Result.success();
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        authService.logout(token);
        return Result.success();
    }
}
