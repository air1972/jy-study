CREATE TABLE IF NOT EXISTS `study_comment` (
  `comment_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `type` char(1) NOT NULL COMMENT '评论类型：1-课程，2-文章',
  `target_id` bigint(20) NOT NULL COMMENT '目标ID',
  `content` varchar(1000) NOT NULL COMMENT '评论内容',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态：0-正常，1-隐藏',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`comment_id`),
  KEY `idx_comment_target` (`type`, `target_id`, `status`, `create_time`),
  KEY `idx_comment_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程与文章评论表';
