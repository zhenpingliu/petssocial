package com.petssocial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petssocial.dto.LoginRequest;
import com.petssocial.dto.RegisterRequest;
import com.petssocial.entity.User;
import com.petssocial.entity.VerificationCode;
import com.petssocial.mapper.UserMapper;
import com.petssocial.mapper.VerificationCodeMapper;
import com.petssocial.service.AuthService;
import com.petssocial.util.JwtUtils;
import com.petssocial.vo.AuthResponse;
import com.petssocial.vo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VerificationCodeMapper verificationCodeMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public AuthResponse login(LoginRequest request) {
        // Verify code
        if (!verifyCode(request.getPhone(), request.getCode(), 1)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // Find or create user
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, request.getPhone()));
        
        if (user == null) {
            throw new RuntimeException("用户不存在，请先注册");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }

        // Generate tokens
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getPhone());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getPhone());

        // Store refresh token in Redis
        redisTemplate.opsForValue().set("refresh_token:" + user.getId(), refreshToken, 
                jwtUtils.getClass().getAnnotation(org.springframework.stereotype.Component.class) != null ? 86400 : 86400, TimeUnit.SECONDS);

        // Clear used verification code
        clearUsedCode(request.getPhone(), 1);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        // Verify code
        if (!verifyCode(request.getPhone(), request.getCode(), 2)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // Check if user already exists
        User existUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, request.getPhone()));
        
        if (existUser != null) {
            throw new RuntimeException("用户已存在");
        }

        // Create new user
        User user = new User();
        user.setPhone(request.getPhone());
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : "用户" + request.getPhone().substring(7));
        user.setGender(request.getGender() != null ? request.getGender() : 0);
        user.setStatus(1);
        
        userMapper.insert(user);

        // Generate tokens
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getPhone());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getPhone());

        // Store refresh token in Redis
        redisTemplate.opsForValue().set("refresh_token:" + user.getId(), refreshToken, 86400, TimeUnit.SECONDS);

        // Clear used verification code
        clearUsedCode(request.getPhone(), 2);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtils.isRefreshToken(refreshToken) || jwtUtils.isExpired(refreshToken)) {
            throw new RuntimeException("无效的刷新令牌");
        }

        Long userId = jwtUtils.getUserIdFromToken(refreshToken);
        String phone = jwtUtils.getPhoneFromToken(refreshToken);

        // Verify refresh token in Redis
        String storedToken = redisTemplate.opsForValue().get("refresh_token:" + userId);
        if (!refreshToken.equals(storedToken)) {
            throw new RuntimeException("刷新令牌已失效");
        }

        User user = userMapper.selectById(userId);
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
            throw new RuntimeException("用户不存在或已被禁用");
        }

        // Generate new tokens
        String newAccessToken = jwtUtils.generateAccessToken(userId, phone);
        String newRefreshToken = jwtUtils.generateRefreshToken(userId, phone);

        // Update refresh token in Redis
        redisTemplate.opsForValue().set("refresh_token:" + userId, newRefreshToken, 86400, TimeUnit.SECONDS);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    @Override
    public void sendVerificationCode(String phone, Integer type) {
        // Generate 6-digit code
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // Store code in database
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setPhone(phone);
        verificationCode.setCode(code);
        verificationCode.setType(type);
        verificationCode.setUsed(false);
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        verificationCodeMapper.insert(verificationCode);

        // TODO: Send SMS via SMS service (mock for now)
        log.info("发送验证码: phone={}, code={}, type={}", phone, code, type);

        // For development, also store in Redis for quick verification
        redisTemplate.opsForValue().set("sms_code:" + phone + ":" + type, code, 5, TimeUnit.MINUTES);
    }

    @Override
    public void logout(String token) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token);
            redisTemplate.delete("refresh_token:" + userId);
        } catch (Exception e) {
            log.warn("Logout error: {}", e.getMessage());
        }
    }

    private boolean verifyCode(String phone, String code, Integer type) {
        // First check Redis
        String storedCode = redisTemplate.opsForValue().get("sms_code:" + phone + ":" + type);
        if (code.equals(storedCode)) {
            return true;
        }

        // Then check database
        VerificationCode verificationCode = verificationCodeMapper.selectOne(
                new LambdaQueryWrapper<VerificationCode>()
                        .eq(VerificationCode::getPhone, phone)
                        .eq(VerificationCode::getCode, code)
                        .eq(VerificationCode::getType, type)
                        .eq(VerificationCode::getUsed, false)
                        .gt(VerificationCode::getExpiresAt, LocalDateTime.now())
                        .orderByDesc(VerificationCode::getCreatedAt)
                        .last("LIMIT 1")
        );
        
        return verificationCode != null;
    }

    private void clearUsedCode(String phone, Integer type) {
        redisTemplate.delete("sms_code:" + phone + ":" + type);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(3600L);

        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(user, userInfo);
        response.setUser(userInfo);

        return response;
    }
}
