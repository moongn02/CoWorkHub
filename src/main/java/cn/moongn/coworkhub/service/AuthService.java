package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.common.vo.LoginVO;
import cn.moongn.coworkhub.model.dto.LoginDTO;
import cn.moongn.coworkhub.model.vo.RegisterVO;
import jakarta.validation.Valid;

import java.util.Map;

public interface AuthService {
    Map<String, Object> login(LoginVO loginVO);
    void register(@Valid RegisterVO loginVO);
    LoginDTO getUser(String username);
    //void sendVerificationCode(String emailOrPhone);
    //void resetPassword(ResetPasswordVO resetPasswordVO);
    void logout();
} 