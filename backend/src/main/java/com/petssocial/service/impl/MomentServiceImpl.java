package com.petssocial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petssocial.dto.CommentCreateRequest;
import com.petssocial.dto.MomentCreateRequest;
import com.petssocial.entity.Comment;
import com.petssocial.entity.Like;
import com.petssocial.entity.Moment;
import com.petssocial.entity.Pet;
import com.petssocial.entity.User;
import com.petssocial.mapper.CommentMapper;
import com.petssocial.mapper.LikeMapper;
import com.petssocial.mapper.MomentMapper;
import com.petssocial.mapper.PetMapper;
import com.petssocial.mapper.UserMapper;
import com.petssocial.service.MomentService;
import com.petssocial.vo.CommentVO;
import com.petssocial.vo.MomentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MomentServiceImpl implements MomentService {

    @Autowired
    private MomentMapper momentMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public MomentVO createMoment(Long userId, MomentCreateRequest request) {
        Moment moment = new Moment();
        moment.setUserId(userId);
        moment.setPetId(request.getPetId());
        moment.setContent(request.getContent());
        moment.setLocationName(request.getLocationName());
        moment.setLongitude(request.getLongitude());
        moment.setLatitude(request.getLatitude());
        moment.setLikeCount(0);
        moment.setCommentCount(0);
        moment.setStatus(1);

        // Serialize images to JSON
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                moment.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (JsonProcessingException e) {
                moment.setImages("[]");
            }
        } else {
            moment.setImages("[]");
        }

        momentMapper.insert(moment);
        return convertToVO(moment, userId);
    }

    @Override
    public IPage<MomentVO> listMoments(Long userId, int page, int size) {
        Page<Moment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Moment> wrapper = new LambdaQueryWrapper<Moment>()
                .orderByDesc(Moment::getCreatedAt);
        IPage<Moment> momentPage = momentMapper.selectPage(pageParam, wrapper);
        return momentPage.convert(m -> convertToVO(m, userId));
    }

    @Override
    public IPage<MomentVO> listUserMoments(Long userId, Long targetUserId, int page, int size) {
        Page<Moment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Moment> wrapper = new LambdaQueryWrapper<Moment>()
                .eq(Moment::getUserId, targetUserId)
                .orderByDesc(Moment::getCreatedAt);
        IPage<Moment> momentPage = momentMapper.selectPage(pageParam, wrapper);
        return momentPage.convert(m -> convertToVO(m, userId));
    }

    @Override
    public void deleteMoment(Long userId, Long momentId) {
        Moment moment = momentMapper.selectById(momentId);
        if (moment == null) {
            throw new RuntimeException("动态不存在");
        }
        if (!moment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此动态");
        }
        momentMapper.deleteById(momentId);
    }

    @Override
    public void likeMoment(Long userId, Long momentId) {
        Moment moment = momentMapper.selectById(momentId);
        if (moment == null) {
            throw new RuntimeException("动态不存在");
        }

        // Check if already liked
        Like existing = likeMapper.selectOne(new LambdaQueryWrapper<Like>()
                .eq(Like::getMomentId, momentId)
                .eq(Like::getUserId, userId));
        if (existing != null) {
            throw new RuntimeException("已经点过赞了");
        }

        Like like = new Like();
        like.setMomentId(momentId);
        like.setUserId(userId);
        likeMapper.insert(like);

        // Update like count
        moment.setLikeCount(moment.getLikeCount() + 1);
        momentMapper.updateById(moment);
    }

    @Override
    public void unlikeMoment(Long userId, Long momentId) {
        Like like = likeMapper.selectOne(new LambdaQueryWrapper<Like>()
                .eq(Like::getMomentId, momentId)
                .eq(Like::getUserId, userId));
        if (like == null) {
            throw new RuntimeException("未点过赞");
        }

        likeMapper.deleteById(like.getId());

        Moment moment = momentMapper.selectById(momentId);
        if (moment != null && moment.getLikeCount() > 0) {
            moment.setLikeCount(moment.getLikeCount() - 1);
            momentMapper.updateById(moment);
        }
    }

    @Override
    public CommentVO addComment(Long userId, Long momentId, CommentCreateRequest request) {
        Moment moment = momentMapper.selectById(momentId);
        if (moment == null) {
            throw new RuntimeException("动态不存在");
        }

        Comment comment = new Comment();
        comment.setMomentId(momentId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        comment.setStatus(1);
        commentMapper.insert(comment);

        // Update comment count
        moment.setCommentCount(moment.getCommentCount() + 1);
        momentMapper.updateById(moment);

        return convertCommentToVO(comment);
    }

    @Override
    public IPage<CommentVO> listComments(Long momentId, int page, int size) {
        Page<Comment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getMomentId, momentId)
                .orderByAsc(Comment::getCreatedAt);
        IPage<Comment> commentPage = commentMapper.selectPage(pageParam, wrapper);
        return commentPage.convert(this::convertCommentToVO);
    }

    private MomentVO convertToVO(Moment moment, Long currentUserId) {
        MomentVO vo = new MomentVO();
        vo.setId(moment.getId());
        vo.setContent(moment.getContent());
        vo.setLocationName(moment.getLocationName());
        vo.setLikeCount(moment.getLikeCount());
        vo.setCommentCount(moment.getCommentCount());
        vo.setCreatedAt(moment.getCreatedAt());

        // Parse images JSON
        try {
            List<String> images = objectMapper.readValue(
                    moment.getImages() != null ? moment.getImages() : "[]",
                    new TypeReference<List<String>>() {});
            vo.setImages(images);
        } catch (JsonProcessingException e) {
            vo.setImages(Collections.emptyList());
        }

        // Check if current user liked
        Like like = likeMapper.selectOne(new LambdaQueryWrapper<Like>()
                .eq(Like::getMomentId, moment.getId())
                .eq(Like::getUserId, currentUserId));
        vo.setLiked(like != null);

        // Get user info
        User user = userMapper.selectById(moment.getUserId());
        if (user != null) {
            MomentVO.UserInfo userInfo = new MomentVO.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setNickname(user.getNickname());
            userInfo.setAvatar(user.getAvatar());
            vo.setUser(userInfo);
        }

        // Get pet info
        if (moment.getPetId() != null) {
            Pet pet = petMapper.selectById(moment.getPetId());
            if (pet != null) {
                MomentVO.PetInfo petInfo = new MomentVO.PetInfo();
                petInfo.setId(pet.getId());
                petInfo.setName(pet.getName());
                petInfo.setAvatar(pet.getAvatar());
                petInfo.setSpecies(pet.getSpecies());
                vo.setPet(petInfo);
            }
        }

        return vo;
    }

    private CommentVO convertCommentToVO(Comment comment) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setMomentId(comment.getMomentId());
        vo.setContent(comment.getContent());
        vo.setParentId(comment.getParentId());
        vo.setCreatedAt(comment.getCreatedAt());

        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            CommentVO.UserInfo userInfo = new CommentVO.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setNickname(user.getNickname());
            userInfo.setAvatar(user.getAvatar());
            vo.setUser(userInfo);
        }

        return vo;
    }
}
