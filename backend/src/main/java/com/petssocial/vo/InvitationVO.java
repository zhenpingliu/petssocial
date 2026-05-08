package com.petssocial.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InvitationVO {
    private Long id;
    private Long inviterId;
    private Long inviterPetId;
    private Long inviteeId;
    private Long inviteePetId;
    private Double longitude;
    private Double latitude;
    private String locationName;
    private LocalDateTime appointmentTime;
    private Integer status;
    private String message;
    private LocalDateTime createdAt;

    private UserInfo inviter;
    private PetInfo inviterPet;
    private UserInfo invitee;
    private PetInfo inviteePet;

    @Data
    public static class UserInfo {
        private Long id;
        private String nickname;
        private String avatar;
    }

    @Data
    public static class PetInfo {
        private Long id;
        private String name;
        private String avatar;
        private Integer species;
    }
}
