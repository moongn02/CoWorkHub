package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.common.constant.RedisConstant;
import cn.moongn.coworkhub.common.utils.EmailUtils;
import cn.moongn.coworkhub.common.utils.RedisUtils;
import cn.moongn.coworkhub.common.utils.VerificationCodeUtils;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class PasswordController {

    private final UserService userService;
    private final EmailUtils emailUtils;
    private final RedisUtils redisUtils;
    private final PasswordEncoder passwordEncoder;

    /**
     * 发送验证码
     */
    @PostMapping("/send_code")
    public Result<String> sendVerificationCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        if (email == null || email.isEmpty()) {
            return Result.error("邮箱不能为空");
        }

        // 校验邮箱格式
        if (!email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            return Result.error("邮箱格式不正确");
        }

        // 校验邮箱是否存在
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return Result.error("该邮箱未注册");
        }

        // 生成6位数验证码
        String code = VerificationCodeUtils.generateNumericCode(6);

        // 发送验证码邮件
        boolean sent = emailUtils.sendVerificationCodeMail(
                email,
                code,
                RedisConstant.VERIFICATION_CODE_EXPIRE_MINUTES
        );

        if (sent) {
            // 将验证码保存到Redis
            String redisKey = RedisConstant.VERIFICATION_CODE_PREFIX + email;
            redisUtils.set(
                    redisKey,
                    code,
                    RedisConstant.VERIFICATION_CODE_EXPIRE_MINUTES,
                    TimeUnit.MINUTES
            );

            return Result.success("验证码发送成功");
        } else {
            return Result.error("验证码发送失败");
        }
    }

    /**
     * 验证验证码并重置密码
     */
    @PostMapping("/reset")
    public Result<String> resetPassword(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");
        String newPassword = params.get("newPassword");

        if (email == null || email.isEmpty()) {
            return Result.error("邮箱不能为空");
        }

        if (code == null || code.isEmpty()) {
            return Result.error("验证码不能为空");
        }

        if (newPassword == null || newPassword.isEmpty()) {
            return Result.error("新密码不能为空");
        }

        // 校验验证码
        String redisKey = RedisConstant.VERIFICATION_CODE_PREFIX + email;
        Object savedCode = redisUtils.get(redisKey);

        if (savedCode == null) {
            return Result.error("验证码已过期");
        }

        if (!code.equals(savedCode.toString())) {
            return Result.error("验证码不正确");
        }

        // 查找用户
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return Result.error("该邮箱未注册");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        boolean updated = userService.updateById(user);

        if (updated) {
            // 删除Redis中的验证码
            redisUtils.delete(redisKey);
            return Result.success("密码重置成功");
        } else {
            return Result.error("密码重置失败");
        }
    }
}