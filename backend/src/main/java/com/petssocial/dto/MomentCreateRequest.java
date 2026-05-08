package com.petssocial.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class MomentCreateRequest {
    @NotBlank(message = "动态内容不能为空")
    private String content;

    private List<String> images;

    private Long petId;

    private Double longitude;

    private Double latitude;

    private String locationName;
}
