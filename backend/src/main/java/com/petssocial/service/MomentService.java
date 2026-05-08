package com.petssocial.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.petssocial.dto.CommentCreateRequest;
import com.petssocial.dto.MomentCreateRequest;
import com.petssocial.vo.CommentVO;
import com.petssocial.vo.MomentVO;

public interface MomentService {
    MomentVO createMoment(Long userId, MomentCreateRequest request);
    IPage<MomentVO> listMoments(Long userId, int page, int size);
    IPage<MomentVO> listUserMoments(Long userId, Long targetUserId, int page, int size);
    void deleteMoment(Long userId, Long momentId);
    void likeMoment(Long userId, Long momentId);
    void unlikeMoment(Long userId, Long momentId);
    CommentVO addComment(Long userId, Long momentId, CommentCreateRequest request);
    IPage<CommentVO> listComments(Long momentId, int page, int size);
}
