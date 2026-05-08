package com.petssocial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petssocial.dto.InvitationCreateRequest;
import com.petssocial.entity.Invitation;
import com.petssocial.entity.Pet;
import com.petssocial.entity.User;
import com.petssocial.mapper.InvitationMapper;
import com.petssocial.mapper.PetMapper;
import com.petssocial.mapper.UserMapper;
import com.petssocial.service.InvitationService;
import com.petssocial.vo.InvitationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvitationServiceImpl implements InvitationService {

    @Autowired
    private InvitationMapper invitationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PetMapper petMapper;

    @Override
    public InvitationVO createInvitation(Long userId, InvitationCreateRequest request) {
        // Verify inviter's pet belongs to user
        Pet inviterPet = petMapper.selectById(request.getInviterPetId());
        if (inviterPet == null || !inviterPet.getUserId().equals(userId)) {
            throw new RuntimeException("邀约宠物不存在或不属于当前用户");
        }

        // If inviting a specific user, verify
        if (request.getInviteeId() != null) {
            User invitee = userMapper.selectById(request.getInviteeId());
            if (invitee == null) {
                throw new RuntimeException("被邀约用户不存在");
            }
        }

        Invitation invitation = new Invitation();
        invitation.setInviterId(userId);
        invitation.setInviterPetId(request.getInviterPetId());
        invitation.setInviteeId(request.getInviteeId());
        invitation.setInviteePetId(request.getInviteePetId());
        invitation.setLongitude(request.getLongitude());
        invitation.setLatitude(request.getLatitude());
        invitation.setLocationName(request.getLocationName());
        invitation.setAppointmentTime(request.getAppointmentTime());
        invitation.setMessage(request.getMessage());
        invitation.setStatus(1); // pending
        invitationMapper.insert(invitation);

        return convertToVO(invitation);
    }

    @Override
    public InvitationVO acceptInvitation(Long userId, Long invitationId) {
        Invitation invitation = invitationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new RuntimeException("邀约不存在");
        }
        if (!userId.equals(invitation.getInviteeId())) {
            throw new RuntimeException("无权操作此邀约");
        }
        if (invitation.getStatus() != 1) {
            throw new RuntimeException("邀约状态不可操作");
        }
        invitation.setStatus(2); // accepted
        invitationMapper.updateById(invitation);
        return convertToVO(invitation);
    }

    @Override
    public InvitationVO rejectInvitation(Long userId, Long invitationId) {
        Invitation invitation = invitationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new RuntimeException("邀约不存在");
        }
        if (!userId.equals(invitation.getInviteeId())) {
            throw new RuntimeException("无权操作此邀约");
        }
        if (invitation.getStatus() != 1) {
            throw new RuntimeException("邀约状态不可操作");
        }
        invitation.setStatus(3); // rejected
        invitationMapper.updateById(invitation);
        return convertToVO(invitation);
    }

    @Override
    public InvitationVO cancelInvitation(Long userId, Long invitationId) {
        Invitation invitation = invitationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new RuntimeException("邀约不存在");
        }
        if (!userId.equals(invitation.getInviterId())) {
            throw new RuntimeException("无权操作此邀约");
        }
        if (invitation.getStatus() != 1) {
            throw new RuntimeException("邀约状态不可操作");
        }
        invitation.setStatus(4); // cancelled
        invitationMapper.updateById(invitation);
        return convertToVO(invitation);
    }

    @Override
    public IPage<InvitationVO> listSentInvitations(Long userId, int page, int size) {
        Page<Invitation> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Invitation> wrapper = new LambdaQueryWrapper<Invitation>()
                .eq(Invitation::getInviterId, userId)
                .orderByDesc(Invitation::getCreatedAt);
        IPage<Invitation> invitationPage = invitationMapper.selectPage(pageParam, wrapper);
        return invitationPage.convert(this::convertToVO);
    }

    @Override
    public IPage<InvitationVO> listReceivedInvitations(Long userId, int page, int size) {
        Page<Invitation> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Invitation> wrapper = new LambdaQueryWrapper<Invitation>()
                .eq(Invitation::getInviteeId, userId)
                .orderByDesc(Invitation::getCreatedAt);
        IPage<Invitation> invitationPage = invitationMapper.selectPage(pageParam, wrapper);
        return invitationPage.convert(this::convertToVO);
    }

    @Override
    public InvitationVO getInvitationById(Long id) {
        Invitation invitation = invitationMapper.selectById(id);
        if (invitation == null) {
            throw new RuntimeException("邀约不存在");
        }
        return convertToVO(invitation);
    }

    private InvitationVO convertToVO(Invitation invitation) {
        InvitationVO vo = new InvitationVO();
        vo.setId(invitation.getId());
        vo.setInviterId(invitation.getInviterId());
        vo.setInviterPetId(invitation.getInviterPetId());
        vo.setInviteeId(invitation.getInviteeId());
        vo.setInviteePetId(invitation.getInviteePetId());
        vo.setLongitude(invitation.getLongitude());
        vo.setLatitude(invitation.getLatitude());
        vo.setLocationName(invitation.getLocationName());
        vo.setAppointmentTime(invitation.getAppointmentTime());
        vo.setStatus(invitation.getStatus());
        vo.setMessage(invitation.getMessage());
        vo.setCreatedAt(invitation.getCreatedAt());

        // Get inviter info
        User inviter = userMapper.selectById(invitation.getInviterId());
        if (inviter != null) {
            InvitationVO.UserInfo inviterInfo = new InvitationVO.UserInfo();
            inviterInfo.setId(inviter.getId());
            inviterInfo.setNickname(inviter.getNickname());
            inviterInfo.setAvatar(inviter.getAvatar());
            vo.setInviter(inviterInfo);
        }

        // Get inviter's pet info
        Pet inviterPet = petMapper.selectById(invitation.getInviterPetId());
        if (inviterPet != null) {
            InvitationVO.PetInfo petInfo = new InvitationVO.PetInfo();
            petInfo.setId(inviterPet.getId());
            petInfo.setName(inviterPet.getName());
            petInfo.setAvatar(inviterPet.getAvatar());
            petInfo.setSpecies(inviterPet.getSpecies());
            vo.setInviterPet(petInfo);
        }

        // Get invitee info
        if (invitation.getInviteeId() != null) {
            User invitee = userMapper.selectById(invitation.getInviteeId());
            if (invitee != null) {
                InvitationVO.UserInfo inviteeInfo = new InvitationVO.UserInfo();
                inviteeInfo.setId(invitee.getId());
                inviteeInfo.setNickname(invitee.getNickname());
                inviteeInfo.setAvatar(invitee.getAvatar());
                vo.setInvitee(inviteeInfo);
            }
        }

        // Get invitee's pet info
        if (invitation.getInviteePetId() != null) {
            Pet inviteePet = petMapper.selectById(invitation.getInviteePetId());
            if (inviteePet != null) {
                InvitationVO.PetInfo petInfo = new InvitationVO.PetInfo();
                petInfo.setId(inviteePet.getId());
                petInfo.setName(inviteePet.getName());
                petInfo.setAvatar(inviteePet.getAvatar());
                petInfo.setSpecies(inviteePet.getSpecies());
                vo.setInviteePet(petInfo);
            }
        }

        return vo;
    }
}
