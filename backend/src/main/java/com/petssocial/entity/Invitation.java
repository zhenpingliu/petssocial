package com.petssocial.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("invitations")
public class Invitation {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long inviterId;

    private Long inviterPetId;

    private Long inviteeId;

    private Long inviteePetId;

    private Double longitude;

    private Double latitude;

    private String locationName;

    private LocalDateTime appointmentTime;

    private Integer status; // 1: pending, 2: accepted, 3: rejected, 4: cancelled

    private String message;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
