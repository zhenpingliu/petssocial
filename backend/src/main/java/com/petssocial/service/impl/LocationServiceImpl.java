package com.petssocial.service.impl;

import com.petssocial.dto.LocationReportRequest;
import com.petssocial.dto.NearbyQueryRequest;
import com.petssocial.entity.Pet;
import com.petssocial.entity.User;
import com.petssocial.entity.UserLocation;
import com.petssocial.mapper.PetMapper;
import com.petssocial.mapper.UserLocationMapper;
import com.petssocial.mapper.UserMapper;
import com.petssocial.service.LocationService;
import com.petssocial.vo.NearbyUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LocationServiceImpl implements LocationService {

    @Autowired
    private UserLocationMapper userLocationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String GEO_KEY = "petssocial:geo:users";

    @Override
    public void reportLocation(Long userId, LocationReportRequest request) {
        // Save to Redis GEO for fast nearby queries
        redisTemplate.opsForGeo().add(GEO_KEY,
                new org.springframework.data.geo.Point(request.getLongitude(), request.getLatitude()),
                String.valueOf(userId));

        // Set TTL for user's location (1 hour)
        redisTemplate.opsForValue().set("user_location:" + userId,
                request.getLongitude() + "," + request.getLatitude(), 1, TimeUnit.HOURS);

        // Save to database for persistence
        UserLocation location = new UserLocation();
        location.setUserId(userId);
        location.setPetId(request.getPetId());
        location.setLongitude(request.getLongitude());
        location.setLatitude(request.getLatitude());
        location.setIsWalking(false);
        userLocationMapper.insert(location);
    }

    @Override
    public void startWalking(Long userId, Long petId, LocationReportRequest request) {
        // Update Redis GEO
        redisTemplate.opsForGeo().add(GEO_KEY,
                new org.springframework.data.geo.Point(request.getLongitude(), request.getLatitude()),
                String.valueOf(userId));

        // Set walking status in Redis
        redisTemplate.opsForValue().set("walking:" + userId, String.valueOf(petId), 2, TimeUnit.HOURS);
        redisTemplate.opsForValue().set("user_location:" + userId,
                request.getLongitude() + "," + request.getLatitude(), 2, TimeUnit.HOURS);

        // Save to database
        UserLocation location = new UserLocation();
        location.setUserId(userId);
        location.setPetId(petId);
        location.setLongitude(request.getLongitude());
        location.setLatitude(request.getLatitude());
        location.setIsWalking(true);
        userLocationMapper.insert(location);
    }

    @Override
    public void stopWalking(Long userId) {
        // Remove walking status from Redis
        redisTemplate.delete("walking:" + userId);
    }

    @Override
    public List<NearbyUserVO> findNearbyUsers(Long userId, NearbyQueryRequest request) {
        // Use Redis GEO for nearby search - search by member key
        org.springframework.data.geo.Distance distance = new org.springframework.data.geo.Distance(
                request.getRadius(), org.springframework.data.geo.Metrics.KILOMETERS);
        org.springframework.data.geo.GeoResults results =
                redisTemplate.opsForGeo().radius(GEO_KEY, String.valueOf(userId), distance);

        if (results == null) {
            return Collections.emptyList();
        }

        List<NearbyUserVO> voList = new ArrayList<>();
        for (Object r : results) {
            org.springframework.data.geo.GeoResult<org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation> gr =
                    (org.springframework.data.geo.GeoResult<org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation>) r;
            String memberName = String.valueOf(gr.getContent().getName());
            if (memberName.equals(String.valueOf(userId))) continue;

            Long nearbyUserId = Long.valueOf(memberName);
            NearbyUserVO vo = new NearbyUserVO();
            vo.setUserId(nearbyUserId);
            vo.setDistance(gr.getDistance().getValue());

            // Get walking status
            Object walkingObj = redisTemplate.opsForValue().get("walking:" + nearbyUserId);
            String walkingPetId = walkingObj != null ? walkingObj.toString() : null;
            vo.setIsWalking(walkingPetId != null);

            // Get user info
            User user = userMapper.selectById(nearbyUserId);
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }

            // Get walking pet info
            if (walkingPetId != null) {
                Pet pet = petMapper.selectById(Long.valueOf(walkingPetId));
                if (pet != null) {
                    NearbyUserVO.PetInfo petInfo = new NearbyUserVO.PetInfo();
                    petInfo.setPetId(pet.getId());
                    petInfo.setPetName(pet.getName());
                    petInfo.setPetAvatar(pet.getAvatar());
                    petInfo.setSpecies(pet.getSpecies());
                    petInfo.setBreed(pet.getBreed());
                    vo.setPet(petInfo);
                }
            }

            voList.add(vo);
            if (voList.size() >= request.getLimit()) break;
        }
        return voList;
    }

    @Override
    public List<NearbyUserVO> findNearbyPets(Long userId, NearbyQueryRequest request) {
        Double lng = request.getLongitude();
        Double lat = request.getLatitude();

        if (lng == null || lat == null) {
            String locationStr = (String) redisTemplate.opsForValue().get("user_location:" + userId);
            if (locationStr != null) {
                String[] parts = locationStr.split(",");
                lng = Double.parseDouble(parts[0]);
                lat = Double.parseDouble(parts[1]);
            } else {
                throw new RuntimeException("请先上报位置信息");
            }
        }

        // Use MySQL Haversine for more accurate nearby pet queries (only walking users)
        List<Map<String, Object>> nearbyWalking = userLocationMapper.findNearbyWalkingUsers(
                lng, lat, request.getRadius(), userId, request.getLimit());

        return nearbyWalking.stream().map(row -> {
            NearbyUserVO vo = new NearbyUserVO();
            vo.setUserId(((Number) row.get("user_id")).longValue());
            vo.setDistance(((Number) row.get("distance")).doubleValue());
            vo.setIsWalking(true);

            if (row.get("longitude") != null) {
                vo.setLongitude(((Number) row.get("longitude")).doubleValue());
            }
            if (row.get("latitude") != null) {
                vo.setLatitude(((Number) row.get("latitude")).doubleValue());
            }

            // Get user info
            User user = userMapper.selectById(vo.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }

            // Get pet info
            Object petIdObj = row.get("pet_id");
            if (petIdObj != null) {
                Pet pet = petMapper.selectById(((Number) petIdObj).longValue());
                if (pet != null) {
                    NearbyUserVO.PetInfo petInfo = new NearbyUserVO.PetInfo();
                    petInfo.setPetId(pet.getId());
                    petInfo.setPetName(pet.getName());
                    petInfo.setPetAvatar(pet.getAvatar());
                    petInfo.setSpecies(pet.getSpecies());
                    petInfo.setBreed(pet.getBreed());
                    vo.setPet(petInfo);
                }
            }

            return vo;
        }).collect(Collectors.toList());
    }
}
