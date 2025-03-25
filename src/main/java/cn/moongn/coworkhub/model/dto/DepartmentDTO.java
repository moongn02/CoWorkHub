package cn.moongn.coworkhub.model.dto;

import lombok.Data;

@Data
public class DepartmentDTO {
    private Long id;             // 部门ID
    private String name;         // 部门名称
    private Long leaderId;       // 部门负责人ID
    private String leaderName;   // 部门负责人姓名
    private Long parentId;       // 上级部门ID
    private String parentName;   // 上级部门名称
    private Integer status;      // 部门状态（0：禁用, 1：启用）
    private String statusText;   // 状态文本
    private String description;  // 部门描述
}
