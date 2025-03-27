package cn.moongn.coworkhub.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("permission")
public class Permission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private Long parentId;
    private Integer type;
    private Boolean isSensitive;
    private Integer status;
}


/*
-- 权限表
CREATE TABLE permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL COMMENT '权限标识符（如task:create）',
    name VARCHAR(50) NOT NULL COMMENT '权限名称',
    parent_id BIGINT COMMENT '上级权限ID',
    type TINYINT COMMENT '权限类型（1：菜单权限，2：按钮权限）',
    is_sensitive BOOLEAN DEFAULT FALSE COMMENT '是否敏感权限（0：否，1：是）',
    status TINYINT DEFAULT 1 COMMENT '状态（0：禁用，1：启用）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 权限表

 */