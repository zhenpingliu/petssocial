package com.petssocial.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationReportRequest {
    @NotNull(message = "经度不能为空")
    private Double longitude;

    @NotNull(message = "纬度不能为空")
    private Double latitude;

    private Long petId;
}
