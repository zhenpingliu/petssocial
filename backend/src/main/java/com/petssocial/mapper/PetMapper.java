package com.petssocial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petssocial.entity.Pet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetMapper extends BaseMapper<Pet> {
}
