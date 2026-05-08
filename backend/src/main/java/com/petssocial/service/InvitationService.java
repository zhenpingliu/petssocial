package com.petssocial.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.petssocial.dto.InvitationCreateRequest;
import com.petssocial.vo.InvitationVO;

public interface InvitationService {
    InvitationVO createInvitation(Long userId, InvitationCreateRequest request);
    InvitationVO acceptInvitation(Long userId, Long invitationId);
    InvitationVO rejectInvitation(Long userId, Long invitationId);
    InvitationVO cancelInvitation(Long userId, Long invitationId);
    IPage<InvitationVO> listSentInvitations(Long userId, int page, int size);
    IPage<InvitationVO> listReceivedInvitations(Long userId, int page, int size);
    InvitationVO getInvitationById(Long id);
}
