package com.petssocial.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PetUpdateRequest {
    private String name;
    private String avatar;
    private Integer species;
    private String breed;
    private Integer gender;
    private LocalDate birthday;
    private BigDecimal weight;
    private String introduction;
}
