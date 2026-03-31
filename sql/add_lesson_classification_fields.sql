-- 课程四维分类字段升级脚本（可重复执行，兼容 MySQL 5.7/8.0）
-- 作用：
-- 1) 给 study_lesson 增加 version/subject/grade/volume 的 id + name 字段
-- 2) 给 study_lesson_exercise 增加 version/subject/grade/volume 的 id + name 字段
-- 用法：直接整段执行

DROP PROCEDURE IF EXISTS p_add_col_if_missing;
DELIMITER $$
CREATE PROCEDURE p_add_col_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_definition TEXT
)
BEGIN
    DECLARE v_exists INT DEFAULT 0;
    DECLARE v_sql TEXT;

    SELECT COUNT(1) INTO v_exists
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND column_name = p_column_name;

    IF v_exists = 0 THEN
        SET v_sql = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN ', p_definition);
        SET @ddl = v_sql;
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL p_add_col_if_missing('study_lesson', 'version_id',   'version_id BIGINT(20) NULL COMMENT ''版本ID'' AFTER description');
CALL p_add_col_if_missing('study_lesson', 'version_name', 'version_name VARCHAR(100) NULL COMMENT ''版本名称'' AFTER version_id');
CALL p_add_col_if_missing('study_lesson', 'subject_id',   'subject_id BIGINT(20) NULL COMMENT ''科目ID'' AFTER version_name');
CALL p_add_col_if_missing('study_lesson', 'subject_name', 'subject_name VARCHAR(100) NULL COMMENT ''科目名称'' AFTER subject_id');
CALL p_add_col_if_missing('study_lesson', 'grade_id',     'grade_id BIGINT(20) NULL COMMENT ''年级ID'' AFTER subject_name');
CALL p_add_col_if_missing('study_lesson', 'grade_name',   'grade_name VARCHAR(100) NULL COMMENT ''年级名称'' AFTER grade_id');
CALL p_add_col_if_missing('study_lesson', 'volume_id',    'volume_id BIGINT(20) NULL COMMENT ''册子ID'' AFTER grade_name');
CALL p_add_col_if_missing('study_lesson', 'volume_name',  'volume_name VARCHAR(100) NULL COMMENT ''册子名称'' AFTER volume_id');

-- 练习表分类字段（保存练习时会写入这些字段）
CALL p_add_col_if_missing('study_lesson_exercise', 'version_id',   'version_id BIGINT(20) NULL COMMENT ''版本ID'' AFTER lesson_id');
CALL p_add_col_if_missing('study_lesson_exercise', 'version_name', 'version_name VARCHAR(100) NULL COMMENT ''版本名称'' AFTER version_id');
CALL p_add_col_if_missing('study_lesson_exercise', 'subject_id',   'subject_id BIGINT(20) NULL COMMENT ''科目ID'' AFTER version_name');
CALL p_add_col_if_missing('study_lesson_exercise', 'subject_name', 'subject_name VARCHAR(100) NULL COMMENT ''科目名称'' AFTER subject_id');
CALL p_add_col_if_missing('study_lesson_exercise', 'grade_id',     'grade_id BIGINT(20) NULL COMMENT ''年级ID'' AFTER subject_name');
CALL p_add_col_if_missing('study_lesson_exercise', 'grade_name',   'grade_name VARCHAR(100) NULL COMMENT ''年级名称'' AFTER grade_id');
CALL p_add_col_if_missing('study_lesson_exercise', 'volume_id',    'volume_id BIGINT(20) NULL COMMENT ''册子ID'' AFTER grade_name');
CALL p_add_col_if_missing('study_lesson_exercise', 'volume_name',  'volume_name VARCHAR(100) NULL COMMENT ''册子名称'' AFTER volume_id');

DROP PROCEDURE IF EXISTS p_add_col_if_missing;

-- 可选索引（数据量大时建议执行）
-- CREATE INDEX idx_study_lesson_version_name ON study_lesson(version_name);
-- CREATE INDEX idx_study_lesson_subject_name ON study_lesson(subject_name);
-- CREATE INDEX idx_study_lesson_grade_name   ON study_lesson(grade_name);
-- CREATE INDEX idx_study_lesson_volume_name  ON study_lesson(volume_name);
-- CREATE INDEX idx_study_lesson_exercise_version_id ON study_lesson_exercise(version_id);
-- CREATE INDEX idx_study_lesson_exercise_subject_id ON study_lesson_exercise(subject_id);
-- CREATE INDEX idx_study_lesson_exercise_grade_id   ON study_lesson_exercise(grade_id);
-- CREATE INDEX idx_study_lesson_exercise_volume_id  ON study_lesson_exercise(volume_id);

-- 执行后可检查：
-- SELECT column_name FROM information_schema.columns
-- WHERE table_schema = DATABASE() AND table_name = 'study_lesson'
--   AND column_name IN ('version_id','version_name','subject_id','subject_name','grade_id','grade_name','volume_id','volume_name');
--
-- SELECT column_name FROM information_schema.columns
-- WHERE table_schema = DATABASE() AND table_name = 'study_lesson_exercise'
--   AND column_name IN ('version_id','version_name','subject_id','subject_name','grade_id','grade_name','volume_id','volume_name');
