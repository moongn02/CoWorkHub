package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;

    @GetMapping("/info")
    public Result<User> getUserInfo() {
        // 从认证上下文获取当前用户
        User user = userService.getCurrentUser();

        if (user != null) {
            // 出于安全考虑，清除密码信息
            user.setPassword(null);
            return Result.success(user);
        } else {
            return Result.error(401, "用户未认证或不存在");
        }
    }
}