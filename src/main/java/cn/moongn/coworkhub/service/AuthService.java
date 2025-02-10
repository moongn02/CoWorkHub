package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.common.vo.LoginVO;
import cn.moongn.coworkhub.common.vo.ResetPasswordVO;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Map;

public interface AuthService {
    Map<String, Object> login(LoginVO loginVO);
    void sendVerificationCode(String emailOrPhone);
    void resetPassword(ResetPasswordVO resetPasswordVO);
    void logout();
} 