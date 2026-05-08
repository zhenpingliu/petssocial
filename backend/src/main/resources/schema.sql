-- PetsSocial Database Schema
-- MySQL 8.0+

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. Users Table (用户表)
-- ============================================
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT NOT NULL COMMENT '用户ID',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `gender` SMALLINT DEFAULT 0 COMMENT '性别: 0未知, 1男, 2女',
    `password` VARCHAR(100) DEFAULT NULL COMMENT '密码(预留)',
    `status` SMALLINT DEFAULT 1 COMMENT '状态: 0禁用, 1正常',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` SMALLINT DEFAULT 0 COMMENT '逻辑删除: 0未删除, 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_phone` (`phone`),
    KEY `idx_users_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. Pets Table (宠物表)
-- ============================================
CREATE TABLE IF NOT EXISTS `pets` (
    `id` BIGINT NOT NULL COMMENT '宠物ID',
    `user_id` BIGINT NOT NULL COMMENT '主人用户ID',
    `name` VARCHAR(50) NOT NULL COMMENT '宠物名称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '宠物头像URL',
    `species` SMALLINT DEFAULT 1 COMMENT '物种: 1狗, 2猫, 3其他',
    `breed` VARCHAR(50) DEFAULT NULL COMMENT '品种',
    `gender` SMALLINT DEFAULT 0 COMMENT '性别: 0未知, 1公, 2母',
    `birthday` DATE DEFAULT NULL COMMENT '生日',
    `weight` DECIMAL(5,2) DEFAULT NULL COMMENT '体重(kg)',
    `introduction` VARCHAR(500) DEFAULT NULL COMMENT '简介',
    `status` SMALLINT DEFAULT 1 COMMENT '状态: 0禁用, 1正常',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` SMALLINT DEFAULT 0 COMMENT '逻辑删除: 0未删除, 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_pets_user_id` (`user_id`),
    KEY `idx_pets_deleted` (`deleted`),
    CONSTRAINT `fk_pets_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物表';

-- ============================================
-- 3. Moments Table (宠物动态表)
-- ============================================
CREATE TABLE IF NOT EXISTS `moments` (
    `id` BIGINT NOT NULL COMMENT '动态ID',
    `user_id` BIGINT NOT NULL COMMENT '发布用户ID',
    `pet_id` BIGINT DEFAULT NULL COMMENT '关联宠物ID',
    `content` TEXT COMMENT '动态内容',
    `images` TEXT COMMENT '图片URL数组(JSON)',
    `longitude` DOUBLE DEFAULT NULL COMMENT '经度',
    `latitude` DOUBLE DEFAULT NULL COMMENT '纬度',
    `location_name` VARCHAR(200) DEFAULT NULL COMMENT '位置名称',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `status` SMALLINT DEFAULT 1 COMMENT '状态: 0删除, 1正常',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` SMALLINT DEFAULT 0 COMMENT '逻辑删除: 0未删除, 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_moments_user_id` (`user_id`),
    KEY `idx_moments_pet_id` (`pet_id`),
    KEY `idx_moments_created_at` (`created_at` DESC),
    KEY `idx_moments_deleted` (`deleted`),
    CONSTRAINT `fk_moments_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_moments_pet_id` FOREIGN KEY (`pet_id`) REFERENCES `pets` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物动态表';

-- ============================================
-- 4. Comments Table (评论表)
-- ============================================
CREATE TABLE IF NOT EXISTS `comments` (
    `id` BIGINT NOT NULL COMMENT '评论ID',
    `moment_id` BIGINT NOT NULL COMMENT '动态ID',
    `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID(用于回复)',
    `status` SMALLINT DEFAULT 1 COMMENT '状态: 0删除, 1正常',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` SMALLINT DEFAULT 0 COMMENT '逻辑删除: 0未删除, 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_comments_moment_id` (`moment_id`),
    KEY `idx_comments_user_id` (`user_id`),
    KEY `idx_comments_parent_id` (`parent_id`),
    KEY `idx_comments_deleted` (`deleted`),
    CONSTRAINT `fk_comments_moment_id` FOREIGN KEY (`moment_id`) REFERENCES `moments` (`id`),
    CONSTRAINT `fk_comments_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ============================================
-- 5. Likes Table (点赞表)
-- ============================================
CREATE TABLE IF NOT EXISTS `likes` (
    `id` BIGINT NOT NULL COMMENT '点赞ID',
    `moment_id` BIGINT NOT NULL COMMENT '动态ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_likes_moment_user` (`moment_id`, `user_id`),
    KEY `idx_likes_moment_id` (`moment_id`),
    KEY `idx_likes_user_id` (`user_id`),
    CONSTRAINT `fk_likes_moment_id` FOREIGN KEY (`moment_id`) REFERENCES `moments` (`id`),
    CONSTRAINT `fk_likes_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- ============================================
-- 6. Invitations Table (遛宠邀约表)
-- ============================================
CREATE TABLE IF NOT EXISTS `invitations` (
    `id` BIGINT NOT NULL COMMENT '邀约ID',
    `inviter_id` BIGINT NOT NULL COMMENT '发起人ID',
    `inviter_pet_id` BIGINT NOT NULL COMMENT '发起人宠物ID',
    `invitee_id` BIGINT DEFAULT NULL COMMENT '受邀人ID(公开邀约时为NULL)',
    `invitee_pet_id` BIGINT DEFAULT NULL COMMENT '受邀人宠物ID',
    `longitude` DOUBLE DEFAULT NULL COMMENT '地点经度',
    `latitude` DOUBLE DEFAULT NULL COMMENT '地点纬度',
    `location_name` VARCHAR(200) DEFAULT NULL COMMENT '地点名称',
    `appointment_time` DATETIME NOT NULL COMMENT '约定时间',
    `status` SMALLINT DEFAULT 1 COMMENT '状态: 1待处理, 2已接受, 3已拒绝, 4已取消',
    `message` VARCHAR(500) DEFAULT NULL COMMENT '留言',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` SMALLINT DEFAULT 0 COMMENT '逻辑删除: 0未删除, 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_invitations_inviter_id` (`inviter_id`),
    KEY `idx_invitations_invitee_id` (`invitee_id`),
    KEY `idx_invitations_status` (`status`),
    KEY `idx_invitations_deleted` (`deleted`),
    CONSTRAINT `fk_invitations_inviter_id` FOREIGN KEY (`inviter_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_invitations_inviter_pet_id` FOREIGN KEY (`inviter_pet_id`) REFERENCES `pets` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='遛宠邀约表';

-- ============================================
-- 7. User Locations Table (用户位置跟踪)
-- ============================================
CREATE TABLE IF NOT EXISTS `user_locations` (
    `id` BIGINT NOT NULL COMMENT '位置记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `pet_id` BIGINT DEFAULT NULL COMMENT '宠物ID',
    `longitude` DOUBLE NOT NULL COMMENT '经度',
    `latitude` DOUBLE NOT NULL COMMENT '纬度',
    `is_walking` TINYINT(1) DEFAULT 0 COMMENT '是否正在遛宠',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_locations_user_id` (`user_id`),
    KEY `idx_user_locations_created_at` (`created_at` DESC),
    CONSTRAINT `fk_user_locations_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户位置跟踪表';

-- ============================================
-- 8. Verification Codes Table (验证码表)
-- ============================================
CREATE TABLE IF NOT EXISTS `verification_codes` (
    `id` BIGINT NOT NULL COMMENT '验证码ID',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `code` VARCHAR(10) NOT NULL COMMENT '验证码',
    `type` SMALLINT DEFAULT 1 COMMENT '类型: 1登录, 2注册',
    `used` TINYINT(1) DEFAULT 0 COMMENT '是否已使用',
    `expires_at` DATETIME NOT NULL COMMENT '过期时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_verification_codes_phone` (`phone`),
    KEY `idx_verification_codes_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- Insert sample data
-- ============================================
INSERT IGNORE INTO `users` (`id`, `phone`, `nickname`, `avatar`, `gender`)
VALUES (1, '13800138000', 'Test User', 'https://example.com/avatar.jpg', 1);

INSERT IGNORE INTO `pets` (`id`, `user_id`, `name`, `species`, `breed`, `gender`)
VALUES (1, 1, 'Buddy', 1, 'Golden Retriever', 1);
