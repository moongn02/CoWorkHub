package cn.moongn.coworkhub.model.dto;

import lombok.Data;

@Data
public class ProjectDTO {
    private Long id;                 // 项目ID
    private String name;             // 项目名称
    private Long parentId;           // 上级项目ID
    private String parentName;       // 上级项目名称
    private Long departmentId;       // 所属部门ID
    private String departmentName;   // 所属部门名称
    private Long updaterId;          // 项目最后更新人ID
    private String updaterName;      // 项目最后更新人名称
    private Integer status;          // 项目状态（0：禁用, 1：启用）
    private String statusText;       // 项目状态文本
}
