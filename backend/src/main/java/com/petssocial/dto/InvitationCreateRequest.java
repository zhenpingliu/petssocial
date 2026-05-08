package com.petssocial.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InvitationCreateRequest {
    @NotNull(message = "邀约宠物不能为空")
    private Long inviterPetId;

    private Long inviteeId;

    private Long inviteePetId;

    @NotNull(message = "邀约时间不能为空")
    private LocalDateTime appointmentTime;

    private Double longitude;

    private Double latitude;

    private String locationName;

    private String message;
}
