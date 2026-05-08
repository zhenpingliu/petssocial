package com.petssocial.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.petssocial.dto.InvitationCreateRequest;
import com.petssocial.security.SecurityUtils;
import com.petssocial.service.InvitationService;
import com.petssocial.vo.InvitationVO;
import com.petssocial.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invitation")
@Tag(name = "遛宠邀约", description = "遛宠邀约相关接口")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @PostMapping
    @Operation(summary = "发送邀约")
    public Result<InvitationVO> createInvitation(@Valid @RequestBody InvitationCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(invitationService.createInvitation(userId, request));
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "接受邀约")
    public Result<InvitationVO> acceptInvitation(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(invitationService.acceptInvitation(userId, id));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "拒绝邀约")
    public Result<InvitationVO> rejectInvitation(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(invitationService.rejectInvitation(userId, id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消邀约")
    public Result<InvitationVO> cancelInvitation(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(invitationService.cancelInvitation(userId, id));
    }

    @GetMapping("/sent")
    @Operation(summary = "获取发出的邀约列表")
    public Result<IPage<InvitationVO>> listSentInvitations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(invitationService.listSentInvitations(userId, page, size));
    }

    @GetMapping("/received")
    @Operation(summary = "获取收到的邀约列表")
    public Result<IPage<InvitationVO>> listReceivedInvitations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(invitationService.listReceivedInvitations(userId, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取邀约详情")
    public Result<InvitationVO> getInvitation(@PathVariable Long id) {
        return Result.success(invitationService.getInvitationById(id));
    }
}
