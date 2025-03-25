package cn.moongn.coworkhub.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("department")
public class Department {
    @TableId(type = IdType.AUTO)
    private Long id;             // 部门ID
    private String name;         // 部门名称
    private Long leaderId;       // 部门负责人ID
    private Long parentId;       // 上级部门ID
    private Integer status;      // 部门状态（0：禁用, 1：启用）
    private String description;  // 部门描述
}

/*
SQL建表语句：
CREATE TABLE `t_department` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `name` VARCHAR(50) NOT NULL COMMENT '部门名称',
  `leader_id` BIGINT COMMENT '部门负责人ID',
  `parent_id` BIGINT DEFAULT 0 COMMENT '上级部门ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '部门状态（0：禁用，1：启用）',
  `description` VARCHAR(255) COMMENT '部门描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';
数据：
INSERT INTO `t_department` (`name`, `leader_id`, `parent_id`, `status`, `description`)
VALUES
('技术部', 1, 0, 1, '负责公司技术研发工作'),
('产品部', 2, 0, 1, '负责公司产品设计和规划'),
('前端组', 3, 1, 1, '负责前端开发工作'),
('后端组', 4, 1, 1, '负责后端开发工作');
 */