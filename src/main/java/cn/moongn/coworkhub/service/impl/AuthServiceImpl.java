 package cn.moongn.coworkhub.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.moongn.coworkhub.common.utils.JwtUtils;
import cn.moongn.coworkhub.common.exception.ApiException;
import cn.moongn.coworkhub.model.vo.LoginVO;
import cn.moongn.coworkhub.mapper.UserMapper;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.LoginDTO;
import cn.moongn.coworkhub.model.vo.RegisterVO;
import cn.moongn.coworkhub.service.AuthService;
import cn.moongn.coworkhub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;

    @Autowired
    public AuthServiceImpl(PasswordEncoder passwordEncoder,
                           UserService userService,
                           JwtUtils jwtUtils,
                           UserMapper userMapper) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public void register(RegisterVO registerVO) {
        // 检查用户名是否已存在
        if (getUser(registerVO.getUsername()) != null) {
            throw new ApiException("用户名已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerVO.getUsername());
        user.setPassword(passwordEncoder.encode(registerVO.getPassword()));

        // 保存用户
        userService.save(user);
    }

    @Override
    public LoginDTO getUser(String username) {
        User user = userMapper.findByUsername(username);

        return BeanUtil.copyProperties(user, LoginDTO.class);
    }

    @Override
    public Map<String, Object> login(LoginVO loginVO) {
        LoginDTO user = getUser(loginVO.getUsername());
        if (user != null && passwordEncoder.matches(loginVO.getPassword(), user.getPassword())) {
            String token = jwtUtils.generateToken(user.getUsername());
            Map<String, Object> result = new HashMap<>();
            user.setPassword(null);
            result.put("token", token);
            result.put("user", user);
            return result;
        }
        throw new ApiException("用户名或密码错误");
    }

//    @Override
//    public void sendVerificationCode(String emailOrPhone) {
//        // 生成并发送验证码逻辑
//        String code = String.valueOf((int) (Math.random() * 900000) + 100000); // 生成六位验证码
//        verificationCodes.put(emailOrPhone, code);
//        // 发送验证码到邮箱或手机号的逻辑
//    }
//
//    @Override
//    public void resetPassword(ResetPasswordVO resetPasswordVO) {
//        String storedCode = verificationCodes.get(resetPasswordVO.getEmailOrPhone());
//        if (storedCode == null || !storedCode.equals(resetPasswordVO.getVerificationCode())) {
//            throw new ApiException("验证码错误");
//        }
//
//        User user = userService.getByUsername(resetPasswordVO.getEmailOrPhone());
//        if (user == null) {
//            throw new ApiException("用户不存在");
//        }
//
//        user.setPassword(passwordEncoder.encode(resetPasswordVO.getNewPassword())); // 使用密码加密
//        userService.update(user);
//        verificationCodes.remove(resetPasswordVO.getEmailOrPhone()); // 清除验证码
//    }

    public void logout() {
        // 可以在这里添加一些清理工作，比如清除用户的缓存等
        SecurityContextHolder.clearContext();
    }
}