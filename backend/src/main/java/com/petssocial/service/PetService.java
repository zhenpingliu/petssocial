package com.petssocial.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.petssocial.entity.Pet;

public interface PetService {
    Pet createPet(Long userId, com.petssocial.dto.PetCreateRequest request);
    Pet updatePet(Long userId, Long petId, com.petssocial.dto.PetUpdateRequest request);
    void deletePet(Long userId, Long petId);
    Pet getPetById(Long petId);
    IPage<Pet> listPetsByUserId(Long userId, int page, int size);
}
