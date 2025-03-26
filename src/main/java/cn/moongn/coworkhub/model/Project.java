package cn.moongn.coworkhub.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("project")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;             // 项目ID
    private String name;         // 项目名称
    private Long parentId;       // 上级项目ID
    private Long departmentId;   // 所属部门ID
    private Long updaterId;      // 项目最后更新人ID
    private Integer status;      // 项目状态（0：禁用, 1：启用）
}

/*
建表语句：
CREATE TABLE `project` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '项目ID',
    `name` VARCHAR(255) NOT NULL COMMENT '项目名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '上级项目ID，0表示顶级项目',
    `department_id` BIGINT COMMENT '所属部门ID',
    `updater_id` BIGINT COMMENT '项目最后更新人ID',
    `status` TINYINT DEFAULT 1 COMMENT '项目状态（0：禁用, 1：启用）',
    PRIMARY KEY (`id`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';
插入项目数据：
INSERT INTO `project` (`name`, `parent_id`, `department_id`, `updater_id`, `status`) VALUES
-- 技术部门项目
('企业官网重构项目', 0, 1, 1, 1),
('移动端APP开发', 0, 1, 2, 1),
('数据中台建设', 0, 1, 3, 1),
('微服务架构升级', 3, 1, 1, 1),
('DevOps流程优化', 3, 1, 2, 1),
-- 市场部门项目
('2023年品牌推广计划', 0, 2, 4, 1),
('社交媒体营销活动', 6, 2, 5, 1),
('线下展会策划', 6, 2, 4, 1),
('市场调研分析', 0, 2, 5, 0),  -- 禁用
-- 产品部门项目
('用户体验优化', 0, 3, 6, 1),
('产品功能迭代V2.0', 10, 3, 7, 1),
('竞品分析报告', 10, 3, 6, 1),
('新产品概念设计', 0, 3, 7, 0),  -- 禁用
-- 财务部门项目
('财务系统升级', 0, 4, 8, 1),
('预算管理优化', 14, 4, 9, 1),
('成本控制方案', 0, 4, 8, 1),
-- 人力资源部门项目
('人才招聘计划2023', 0, 5, 10, 1),
('员工培训体系建设', 17, 5, 11, 1),
('绩效管理系统优化', 17, 5, 10, 1),
('企业文化建设', 0, 5, 11, 0);  -- 禁用
 */