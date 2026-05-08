package com.petssocial.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("verification_codes")
public class VerificationCode {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String phone;

    private String code;

    private Integer type; // 1: login, 2: register

    private Boolean used;

    @TableField("expires_at")
    private LocalDateTime expiresAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
