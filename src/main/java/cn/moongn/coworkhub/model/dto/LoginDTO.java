package cn.moongn.coworkhub.model.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private Long id;
    private String username;
    private String password;
    private String role;
}
