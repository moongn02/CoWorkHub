package cn.moongn.coworkhub.model.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private LocalDate birthday;
    private Integer gender;
    private String genderText;
    private Long deptId;
    private String deptText;
    private String supervisor;
    private Long roleId;
    private String roleName;
    private Integer status;
}