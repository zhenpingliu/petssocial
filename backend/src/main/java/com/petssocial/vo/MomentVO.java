package com.petssocial.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MomentVO {
    private Long id;
    private String content;
    private List<String> images;
    private String locationName;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean liked;
    private LocalDateTime createdAt;

    private UserInfo user;
    private PetInfo pet;

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
