package com.petssocial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petssocial.entity.UserLocation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserLocationMapper extends BaseMapper<UserLocation> {

    /**
     * 查找附近正在遛宠的用户 (MySQL Haversine 球面距离公式)
     * 返回距离单位为米
     */
    @Select("SELECT ul.user_id, ul.pet_id, ul.is_walking, " +
            "ul.longitude, ul.latitude, " +
            "6371000 * ACOS( " +
            "  COS(RADIANS(#{lat})) * COS(RADIANS(ul.latitude)) * " +
            "  COS(RADIANS(#{lng}) - RADIANS(ul.longitude)) + " +
            "  SIN(RADIANS(#{lat})) * SIN(RADIANS(ul.latitude)) " +
            ") AS distance " +
            "FROM user_locations ul " +
            "WHERE ul.is_walking = 1 " +
            "AND ul.created_at > NOW() - INTERVAL 1 HOUR " +
            "AND ul.user_id != #{currentUserId} " +
            "HAVING distance <= #{radius} * 1000 " +
            "ORDER BY distance ASC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> findNearbyWalkingUsers(@Param("lng") Double lng, @Param("lat") Double lat,
                                                      @Param("radius") Double radiusKm,
                                                      @Param("currentUserId") Long currentUserId,
                                                      @Param("limit") Integer limit);
}
