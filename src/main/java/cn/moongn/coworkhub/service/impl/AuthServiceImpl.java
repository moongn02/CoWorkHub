 package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.common.utils.JwtUtils;
import cn.moongn.coworkhub.common.exception.ApiException;
import cn.moongn.coworkhub.common.vo.LoginVO;
import cn.moongn.coworkhub.common.vo.ResetPasswordVO;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.service.AuthService;
import cn.moongn.coworkhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, String> verificationCodes = new HashMap<>();
    
    @Override
    public Map<String, Object> login(LoginVO loginVO) {
        User user = userService.getByUsername(loginVO.getUsername());
        
        // 如果用户不存在，自动注册
        if (user == null) {
            user = new User();
            user.setUsername(loginVO.getUsername());
            user.setPassword(passwordEncoder.encode(loginVO.getPassword()));
            user.setStatus(1); // 默认启用
            user = userService.saveUser(user);
        } else {
            // 用户存在，验证密码
            if (!passwordEncoder.matches(loginVO.getPassword(), user.getPassword())) {
                throw new ApiException("密码错误");
            }
        }
        
        // 生成token
        String token = jwtUtils.generateToken(user.getUsername());
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    @Override
    public void sendVerificationCode(String emailOrPhone) {
        // 生成并发送验证码逻辑
        String code = String.valueOf((int) (Math.random() * 900000) + 100000); // 生成六位验证码
        verificationCodes.put(emailOrPhone, code);
        // 发送验证码到邮箱或手机号的逻辑
    }

    @Override
    public void resetPassword(ResetPasswordVO resetPasswordVO) {
        String storedCode = verificationCodes.get(resetPasswordVO.getEmailOrPhone());
        if (storedCode == null || !storedCode.equals(resetPasswordVO.getVerificationCode())) {
            throw new ApiException("验证码错误");
        }
        
        User user = userService.getByUsername(resetPasswordVO.getEmailOrPhone());
        if (user == null) {
            throw new ApiException("用户不存在");
        }
        
        user.setPassword(passwordEncoder.encode(resetPasswordVO.getNewPassword())); // 使用密码加密
        userService.update(user);
        verificationCodes.remove(resetPasswordVO.getEmailOrPhone()); // 清除验证码
    }

    public void logout() {
        // 可以在这里添加一些清理工作，比如清除用户的缓存等
        SecurityContextHolder.clearContext();
    }
}