package com.petssocial.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pets")
public class Pet {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String name;

    private String avatar;

    private Integer species; // 1: dog, 2: cat, 3: other

    private String breed;

    private Integer gender; // 0: unknown, 1: male, 2: female

    private LocalDate birthday;

    private BigDecimal weight;

    private String introduction;

    private Integer status; // 0: disabled, 1: active

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
