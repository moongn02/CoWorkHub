package cn.moongn.coworkhub.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.ASSIGN_UUID)
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private Long deptId;
    private int gender;
    private LocalDate birthday;
    private Integer roleId;
    private Integer status;
}