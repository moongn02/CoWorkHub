package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.common.vo.LoginVO;
import cn.moongn.coworkhub.common.vo.ResetPasswordVO;
import cn.moongn.coworkhub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginVO loginVO) {
        return Result.success(authService.login(loginVO));
    }

//    @PostMapping("/send-verification-code")
//    public Result<Void> sendVerificationCode(@RequestParam String emailOrPhone) {
//        authService.sendVerificationCode(emailOrPhone);
//        return Result.success();
//    }

//    @PostMapping("/reset-password")
//    public Result<Void> resetPassword(@RequestBody ResetPasswordVO resetPasswordVO) {
//        authService.resetPassword(resetPasswordVO);
//        return Result.success();
//    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
} 