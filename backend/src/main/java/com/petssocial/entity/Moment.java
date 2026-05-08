package com.petssocial.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("moments")
public class Moment {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long petId;

    private String content;

    private String images; // JSON array

    private Double longitude;

    private Double latitude;

    private String locationName;

    private Integer likeCount;

    private Integer commentCount;

    private Integer status; // 0: deleted, 1: active

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
