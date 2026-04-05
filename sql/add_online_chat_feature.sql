CREATE TABLE IF NOT EXISTS `study_online_chat_room` (
  `room_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '房间ID',
  `room_code` varchar(64) NOT NULL COMMENT '房间编码',
  `room_name` varchar(64) NOT NULL COMMENT '房间名称',
  `owner_user_id` bigint(20) NOT NULL COMMENT '群主用户ID',
  `owner_login_name` varchar(64) NOT NULL COMMENT '群主登录账号',
  `owner_user_name` varchar(64) NOT NULL COMMENT '群主显示昵称',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1关闭）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`room_id`),
  UNIQUE KEY `uk_chat_room_code` (`room_code`),
  KEY `idx_chat_room_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线聊天室房间表';

CREATE TABLE IF NOT EXISTS `study_online_chat_room_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_code` varchar(64) NOT NULL COMMENT '房间编码',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `login_name` varchar(64) NOT NULL COMMENT '登录账号',
  `user_name` varchar(64) NOT NULL COMMENT '显示昵称',
  `role` varchar(16) NOT NULL DEFAULT 'member' COMMENT '角色（owner/admin/member）',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1移除）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chat_room_user` (`room_code`, `user_id`),
  KEY `idx_chat_room_user_role` (`room_code`, `role`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线聊天室房间成员表';

CREATE TABLE IF NOT EXISTS `study_online_chat_message` (
  `message_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `room_code` varchar(64) NOT NULL DEFAULT 'public-room' COMMENT '房间编码',
  `user_id` bigint(20) NOT NULL COMMENT '发送用户ID',
  `login_name` varchar(64) NOT NULL COMMENT '登录账号',
  `user_name` varchar(64) NOT NULL COMMENT '显示昵称',
  `reply_message_id` bigint(20) DEFAULT NULL COMMENT '回复消息ID',
  `reply_user_name` varchar(64) DEFAULT NULL COMMENT '被回复用户昵称',
  `reply_content` varchar(255) DEFAULT NULL COMMENT '被回复消息摘要',
  `content` varchar(1000) NOT NULL COMMENT '消息内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`message_id`),
  KEY `idx_room_time` (`room_code`, `create_time`),
  KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线聊天室消息表';

SET @stmt = IF (
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'study_online_chat_message'
      AND COLUMN_NAME = 'reply_message_id'
  ),
  'SELECT 1',
  'ALTER TABLE `study_online_chat_message` ADD COLUMN `reply_message_id` bigint(20) DEFAULT NULL COMMENT ''回复消息ID'' AFTER `user_name`'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @stmt = IF (
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'study_online_chat_message'
      AND COLUMN_NAME = 'reply_user_name'
  ),
  'SELECT 1',
  'ALTER TABLE `study_online_chat_message` ADD COLUMN `reply_user_name` varchar(64) DEFAULT NULL COMMENT ''被回复用户昵称'' AFTER `reply_message_id`'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @stmt = IF (
  EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'study_online_chat_message'
      AND COLUMN_NAME = 'reply_content'
  ),
  'SELECT 1',
  'ALTER TABLE `study_online_chat_message` ADD COLUMN `reply_content` varchar(255) DEFAULT NULL COMMENT ''被回复消息摘要'' AFTER `reply_user_name`'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `study_online_chat_operation_log` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `room_code` varchar(64) NOT NULL COMMENT '房间编码',
  `action_type` varchar(32) NOT NULL COMMENT '操作类型',
  `operator_user_id` bigint(20) NOT NULL COMMENT '操作人用户ID',
  `operator_login_name` varchar(64) NOT NULL COMMENT '操作人登录账号',
  `operator_user_name` varchar(64) NOT NULL COMMENT '操作人昵称',
  `target_user_id` bigint(20) DEFAULT NULL COMMENT '目标用户ID',
  `target_user_name` varchar(64) DEFAULT NULL COMMENT '目标用户昵称',
  `detail` varchar(500) DEFAULT NULL COMMENT '详情',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_chat_op_room_time` (`room_code`, `create_time`),
  KEY `idx_chat_op_operator` (`operator_user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线聊天室管理操作审计日志表';

CREATE TABLE IF NOT EXISTS `study_online_chat_mute` (
  `mute_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '禁言ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `room_code` varchar(64) NOT NULL DEFAULT '*' COMMENT '房间编码，*表示全部房间',
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '禁言开始时间',
  `end_time` datetime NOT NULL COMMENT '禁言结束时间',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0生效 1失效）',
  `reason` varchar(255) DEFAULT NULL COMMENT '禁言原因',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`mute_id`),
  KEY `idx_mute_user_room` (`user_id`, `room_code`, `status`),
  KEY `idx_mute_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线聊天室禁言表';

CREATE TABLE IF NOT EXISTS `study_online_chat_sensitive_word` (
  `word_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '敏感词ID',
  `word` varchar(64) NOT NULL COMMENT '敏感词',
  `replace_text` varchar(64) NOT NULL DEFAULT '**' COMMENT '替换文本',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0生效 1失效）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`word_id`),
  UNIQUE KEY `uk_chat_sensitive_word` (`word`),
  KEY `idx_chat_sensitive_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线聊天室敏感词表';

DELETE FROM `study_online_chat_room_user`
WHERE `room_code` = 'public-room'
  AND `user_id` = 1
  AND `role` = 'owner'
  AND `create_by` = 'system';

DELETE FROM `study_online_chat_room`
WHERE `room_code` = 'public-room'
  AND `owner_user_id` = 1
  AND `owner_login_name` = 'admin'
  AND `create_by` = 'system';

INSERT INTO `study_online_chat_sensitive_word` (`word`, `replace_text`, `status`)
VALUES ('傻逼', '**', '0')
ON DUPLICATE KEY UPDATE `replace_text` = VALUES(`replace_text`), `status` = VALUES(`status`);

INSERT INTO `study_online_chat_sensitive_word` (`word`, `replace_text`, `status`)
VALUES ('操你妈', '**', '0')
ON DUPLICATE KEY UPDATE `replace_text` = VALUES(`replace_text`), `status` = VALUES(`status`);

INSERT INTO `study_online_chat_sensitive_word` (`word`, `replace_text`, `status`)
VALUES ('垃圾', '**', '0')
ON DUPLICATE KEY UPDATE `replace_text` = VALUES(`replace_text`), `status` = VALUES(`status`);
