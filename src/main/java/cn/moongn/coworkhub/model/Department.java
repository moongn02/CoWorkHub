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