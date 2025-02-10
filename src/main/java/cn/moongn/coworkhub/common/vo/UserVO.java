package cn.moongn.coworkhub.common.vo;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private Long departmentId;
    private Integer roleType;
    private Integer status;
} 