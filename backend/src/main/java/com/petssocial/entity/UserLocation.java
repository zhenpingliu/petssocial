package com.petssocial.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_locations")
public class UserLocation {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long petId;

    private Double longitude;

    private Double latitude;

    private Boolean isWalking;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
