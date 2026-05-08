package com.petssocial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petssocial.dto.PetCreateRequest;
import com.petssocial.dto.PetUpdateRequest;
import com.petssocial.entity.Pet;
import com.petssocial.mapper.PetMapper;
import com.petssocial.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PetServiceImpl implements PetService {

    @Autowired
    private PetMapper petMapper;

    @Override
    public Pet createPet(Long userId, PetCreateRequest request) {
        Pet pet = new Pet();
        pet.setUserId(userId);
        pet.setName(request.getName());
        pet.setAvatar(request.getAvatar());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setGender(request.getGender());
        pet.setBirthday(request.getBirthday());
        pet.setWeight(request.getWeight());
        pet.setIntroduction(request.getIntroduction());
        pet.setStatus(1);
        petMapper.insert(pet);
        return pet;
    }

    @Override
    public Pet updatePet(Long userId, Long petId, PetUpdateRequest request) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            throw new RuntimeException("宠物不存在");
        }
        if (!pet.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此宠物");
        }
        if (request.getName() != null) pet.setName(request.getName());
        if (request.getAvatar() != null) pet.setAvatar(request.getAvatar());
        if (request.getSpecies() != null) pet.setSpecies(request.getSpecies());
        if (request.getBreed() != null) pet.setBreed(request.getBreed());
        if (request.getGender() != null) pet.setGender(request.getGender());
        if (request.getBirthday() != null) pet.setBirthday(request.getBirthday());
        if (request.getWeight() != null) pet.setWeight(request.getWeight());
        if (request.getIntroduction() != null) pet.setIntroduction(request.getIntroduction());
        petMapper.updateById(pet);
        return pet;
    }

    @Override
    public void deletePet(Long userId, Long petId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            throw new RuntimeException("宠物不存在");
        }
        if (!pet.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此宠物");
        }
        petMapper.deleteById(petId);
    }

    @Override
    public Pet getPetById(Long petId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            throw new RuntimeException("宠物不存在");
        }
        return pet;
    }

    @Override
    public IPage<Pet> listPetsByUserId(Long userId, int page, int size) {
        Page<Pet> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<Pet>()
                .eq(Pet::getUserId, userId)
                .orderByDesc(Pet::getCreatedAt);
        return petMapper.selectPage(pageParam, wrapper);
    }
}
