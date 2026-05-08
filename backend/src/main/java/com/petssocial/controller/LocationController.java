package com.petssocial.controller;

import com.petssocial.dto.LocationReportRequest;
import com.petssocial.dto.NearbyQueryRequest;
import com.petssocial.security.SecurityUtils;
import com.petssocial.service.LocationService;
import com.petssocial.vo.NearbyUserVO;
import com.petssocial.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/location")
@Tag(name = "位置服务", description = "LBS同城遛宠相关接口")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @PostMapping("/report")
    @Operation(summary = "上报位置")
    public Result<Void> reportLocation(@Valid @RequestBody LocationReportRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        locationService.reportLocation(userId, request);
        return Result.success();
    }

    @PostMapping("/walking/start")
    @Operation(summary = "开始遛宠")
    public Result<Void> startWalking(@Valid @RequestBody LocationReportRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        locationService.startWalking(userId, request.getPetId(), request);
        return Result.success();
    }

    @PostMapping("/walking/stop")
    @Operation(summary = "结束遛宠")
    public Result<Void> stopWalking() {
        Long userId = SecurityUtils.getCurrentUserId();
        locationService.stopWalking(userId);
        return Result.success();
    }

    @GetMapping("/nearby/users")
    @Operation(summary = "发现附近用户")
    public Result<List<NearbyUserVO>> findNearbyUsers(NearbyQueryRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(locationService.findNearbyUsers(userId, request));
    }

    @GetMapping("/nearby/pets")
    @Operation(summary = "发现附近遛宠中的宠物")
    public Result<List<NearbyUserVO>> findNearbyPets(NearbyQueryRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(locationService.findNearbyPets(userId, request));
    }
}
