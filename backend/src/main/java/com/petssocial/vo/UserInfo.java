package com.petssocial.vo;

import lombok.Data;

@Data
public class UserInfo {
    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private Integer gender;
}
