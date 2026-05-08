package com.petssocial.service;

import com.petssocial.dto.LoginRequest;
import com.petssocial.dto.RegisterRequest;
import com.petssocial.vo.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    AuthResponse refreshToken(String refreshToken);
    void sendVerificationCode(String phone, Integer type);
    void logout(String token);
}
