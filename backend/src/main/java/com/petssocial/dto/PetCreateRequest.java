package com.petssocial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PetCreateRequest {
    @NotBlank(message = "宠物名称不能为空")
    private String name;

    private String avatar;

    @NotNull(message = "物种不能为空")
    private Integer species; // 1: dog, 2: cat, 3: other

    private String breed;

    private Integer gender;

    private LocalDate birthday;

    private BigDecimal weight;

    private String introduction;
}
