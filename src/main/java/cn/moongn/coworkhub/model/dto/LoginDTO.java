package cn.moongn.coworkhub.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class LoginDTO {
    private Long id;
    private String username;
    private String password;
    private String role;
    private String realName;
    private Long roleId;
    private String roleName;
    private Integer status;
    private List<String> permissions;
}
