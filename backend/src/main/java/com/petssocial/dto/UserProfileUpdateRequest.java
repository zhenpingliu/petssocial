package com.petssocial.dto;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    private String nickname;
    private Integer gender;
    private String avatar;
}
