package cn.moongn.coworkhub.model.vo;

import lombok.Data;

@Data
public class ResetPasswordVO {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}