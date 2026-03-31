INSERT INTO sys_config (`config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `remark`)
SELECT '评论敏感词', 'study.comment.sensitiveWords', '操你妈,傻逼,妈逼,色情交易,赌博平台,毒品交易,约炮,援交', 'N', 'admin', NOW(), '评论区敏感词，支持逗号或换行分隔'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_config WHERE config_key = 'study.comment.sensitiveWords'
);

INSERT INTO sys_config (`config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `remark`)
SELECT '评论最小发送间隔秒数', 'study.comment.minIntervalSeconds', '15', 'N', 'admin', NOW(), '同一用户评论最小发送间隔，单位秒'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_config WHERE config_key = 'study.comment.minIntervalSeconds'
);

INSERT INTO sys_config (`config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `remark`)
SELECT '评论重复内容判定分钟数', 'study.comment.duplicateMinutes', '10', 'N', 'admin', NOW(), '同一用户在相同内容下重复评论的判定时间窗口，单位分钟'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_config WHERE config_key = 'study.comment.duplicateMinutes'
);

INSERT INTO sys_menu (`menu_id`, `menu_name`, `parent_id`, `order_num`, `url`, `target`, `menu_type`, `visible`, `is_refresh`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 3024, '评论管理', 0, 7, '#', '', 'M', '0', '1', 'study:comment:view', 'fa fa-comments', 'admin', NOW(), '评论管理目录'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE menu_id = 3024 OR menu_name = '评论管理'
);

INSERT INTO sys_menu (`menu_id`, `menu_name`, `parent_id`, `order_num`, `url`, `target`, `menu_type`, `visible`, `is_refresh`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 3025, '评论列表', 3024, 1, '/study/comment', '', 'C', '0', '1', 'study:comment:list', 'fa fa-list', 'admin', NOW(), '评论管理页面'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE menu_id = 3025
);

INSERT INTO sys_menu (`menu_id`, `menu_name`, `parent_id`, `order_num`, `url`, `target`, `menu_type`, `visible`, `is_refresh`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 3026, '评论修改', 3025, 1, '#', '', 'F', '0', '1', 'study:comment:edit', '#', 'admin', NOW(), '评论状态修改'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE menu_id = 3026
);

INSERT INTO sys_menu (`menu_id`, `menu_name`, `parent_id`, `order_num`, `url`, `target`, `menu_type`, `visible`, `is_refresh`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 3027, '评论删除', 3025, 2, '#', '', 'F', '0', '1', 'study:comment:remove', '#', 'admin', NOW(), '评论删除'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE menu_id = 3027
);
