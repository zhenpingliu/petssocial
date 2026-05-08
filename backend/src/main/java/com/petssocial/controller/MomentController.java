package com.petssocial.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.petssocial.dto.CommentCreateRequest;
import com.petssocial.dto.MomentCreateRequest;
import com.petssocial.security.SecurityUtils;
import com.petssocial.service.MomentService;
import com.petssocial.vo.CommentVO;
import com.petssocial.vo.MomentVO;
import com.petssocial.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/moment")
@Tag(name = "宠物动态", description = "动态发布、点赞、评论相关接口")
public class MomentController {

    @Autowired
    private MomentService momentService;

    @PostMapping
    @Operation(summary = "发布动态")
    public Result<MomentVO> createMoment(@Valid @RequestBody MomentCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(momentService.createMoment(userId, request));
    }

    @GetMapping("/feed")
    @Operation(summary = "获取动态Feed流")
    public Result<IPage<MomentVO>> listMoments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(momentService.listMoments(userId, page, size));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取指定用户的动态")
    public Result<IPage<MomentVO>> listUserMoments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(momentService.listUserMoments(userId, userId, page, size));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除动态")
    public Result<Void> deleteMoment(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        momentService.deleteMoment(userId, id);
        return Result.success();
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "点赞")
    public Result<Void> likeMoment(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        momentService.likeMoment(userId, id);
        return Result.success();
    }

    @DeleteMapping("/{id}/like")
    @Operation(summary = "取消点赞")
    public Result<Void> unlikeMoment(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        momentService.unlikeMoment(userId, id);
        return Result.success();
    }

    @PostMapping("/{id}/comment")
    @Operation(summary = "评论")
    public Result<CommentVO> addComment(@PathVariable Long id,
                                        @Valid @RequestBody CommentCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(momentService.addComment(userId, id, request));
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "获取评论列表")
    public Result<IPage<CommentVO>> listComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(momentService.listComments(id, page, size));
    }
}
