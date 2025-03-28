package cn.moongn.coworkhub.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoleDTO {
    private Long id;
    private String name;
    private String description;
    private Integer status;
    private String statusText;

    // 角色拥有的权限ID列表
    private List<Long> permissionIds;

    // 角色拥有的权限名称列表(文本展示)
    private List<String> permissionsText;
}



