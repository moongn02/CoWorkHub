package cn.moongn.coworkhub.model.dto;

import lombok.Data;

@Data
public class PermissionDTO {
    private Long id;
    private String code;
    private String name;
    private Long parentId;
    private String parentName;
    private Integer type;
    private Boolean isSensitive;
    private Integer status;
    private String statusText;
}

