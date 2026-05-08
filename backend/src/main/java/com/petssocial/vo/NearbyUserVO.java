package com.petssocial.vo;

import lombok.Data;

@Data
public class NearbyUserVO {
    private Long userId;
    private String nickname;
    private String avatar;
    private Double longitude;
    private Double latitude;
    private Double distance; // meters
    private Boolean isWalking;
    private PetInfo pet;

    @Data
    public static class PetInfo {
        private Long petId;
        private String petName;
        private String petAvatar;
        private Integer species;
        private String breed;
    }
}
