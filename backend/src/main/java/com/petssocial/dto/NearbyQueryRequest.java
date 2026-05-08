package com.petssocial.dto;

import lombok.Data;

@Data
public class NearbyQueryRequest {
    private Double longitude;
    private Double latitude;
    private Double radius = 5.0; // km
    private Integer limit = 20;
}
