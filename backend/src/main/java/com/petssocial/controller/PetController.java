package com.petssocial.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.petssocial.dto.PetCreateRequest;
import com.petssocial.dto.PetUpdateRequest;
import com.petssocial.entity.Pet;
import com.petssocial.security.SecurityUtils;
import com.petssocial.service.PetService;
import com.petssocial.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pet")
@Tag(name = "宠物管理", description = "宠物档案相关接口")
public class PetController {

    @Autowired
    private PetService petService;

    @PostMapping
    @Operation(summary = "创建宠物档案")
    public Result<Pet> createPet(@Valid @RequestBody PetCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(petService.createPet(userId, request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新宠物档案")
    public Result<Pet> updatePet(@PathVariable Long id, @RequestBody PetUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(petService.updatePet(userId, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除宠物档案")
    public Result<Void> deletePet(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        petService.deletePet(userId, id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取宠物详情")
    public Result<Pet> getPet(@PathVariable Long id) {
        return Result.success(petService.getPetById(id));
    }

    @GetMapping("/list")
    @Operation(summary = "获取当前用户的宠物列表")
    public Result<IPage<Pet>> listPets(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(petService.listPetsByUserId(userId, page, size));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取指定用户的宠物列表")
    public Result<IPage<Pet>> listPetsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(petService.listPetsByUserId(userId, page, size));
    }
}
