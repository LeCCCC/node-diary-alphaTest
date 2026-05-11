CREATE TABLE `user`  (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
                         `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
                         `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
                         `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像地址',
                         `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `gender` smallint NULL DEFAULT 2 COMMENT '性别',
                         PRIMARY KEY (`id`) USING BTREE,
                         UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `diary`  (
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `user_id` bigint NOT NULL COMMENT '用户id',
                          `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
                          `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '正文',
                          `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '缩略封面',
                          `visibility` tinyint NULL DEFAULT 0 COMMENT '可见性: 0-私密, 1-公开',
                          `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                          PRIMARY KEY (`id`) USING BTREE,
                          INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
                          CONSTRAINT `fk_diary_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `tree_hole` (
    `tree_hole_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '树洞id',
    `user_id` BIGINT NOT NULL COMMENT '发布者id',
    `content` TEXT NOT NULL COMMENT '树洞内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    INDEX `idx_user_id`(`user_id` ASC),
    CONSTRAINT `fk_tree_hole_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='树洞表';

CREATE TABLE `tree_hole_comment` (
    `comment_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论id',
    `tree_hole_id` BIGINT NOT NULL COMMENT '树洞id',
    `user_id` BIGINT NOT NULL COMMENT '评论发布者id',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论发布时间',
    INDEX `idx_tree_hole_id`(`tree_hole_id` ASC),
    INDEX `idx_user_id`(`user_id` ASC),
    CONSTRAINT `fk_tree_hole_comment_tree_hole` FOREIGN KEY (`tree_hole_id`) REFERENCES `tree_hole`(`tree_hole_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT `fk_tree_hole_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='树洞评论表';

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `match_relation`  (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `user_id` bigint NOT NULL COMMENT '发起用户ID',
                                   `matched_user_id` bigint NULL DEFAULT NULL COMMENT '被匹配用户ID',
                                   `status` tinyint NULL DEFAULT 1 COMMENT '匹配状态: 0-待处理, 1-已匹配, 2-已屏蔽',
                                   `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '匹配时间',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   UNIQUE INDEX `idx_user_match_pair`(`user_id` ASC, `matched_user_id` ASC) USING BTREE,
                                   INDEX `idx_matched_user`(`matched_user_id` ASC) USING BTREE,
                                   CONSTRAINT `fk_match_target` FOREIGN KEY (`matched_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                   CONSTRAINT `fk_match_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户匹配关系表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `tag` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `category` VARCHAR(50) NOT NULL COMMENT '所属分类',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_category`(`category` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='兴趣标签表';

CREATE TABLE `user_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `tag_id` INT NOT NULL COMMENT '标签ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选择时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `idx_user_tag`(`user_id` ASC, `tag_id` ASC) USING BTREE,
    INDEX `idx_tag_id`(`tag_id` ASC) USING BTREE,
    CONSTRAINT `fk_user_tag_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT `fk_user_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户标签关联表';

SET FOREIGN_KEY_CHECKS = 1;

