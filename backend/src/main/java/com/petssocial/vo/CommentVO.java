package com.petssocial.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVO {
    private Long id;
    private Long momentId;
    private String content;
    private Long parentId;
    private LocalDateTime createdAt;

    private UserInfo user;

    @Data
    public static class UserInfo {
        private Long id;
        private String nickname;
        private String avatar;
    }
}
