package cn.moongn.coworkhub.model.vo;

import lombok.Data;

@Data
public class ResetPasswordVO {
    private String emailOrPhone;
    private String newPassword;
    private String verificationCode;
}