package com.petssocial.service;

import com.petssocial.dto.LocationReportRequest;
import com.petssocial.dto.NearbyQueryRequest;
import com.petssocial.vo.NearbyUserVO;

import java.util.List;

public interface LocationService {
    void reportLocation(Long userId, LocationReportRequest request);
    void startWalking(Long userId, Long petId, LocationReportRequest request);
    void stopWalking(Long userId);
    List<NearbyUserVO> findNearbyUsers(Long userId, NearbyQueryRequest request);
    List<NearbyUserVO> findNearbyPets(Long userId, NearbyQueryRequest request);
}
