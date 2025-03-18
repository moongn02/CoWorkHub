package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.UserDTO;
import cn.moongn.coworkhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;

    @GetMapping("/info")
    public Result<UserDTO> getUserInfo() {
        // 从认证上下文获取当前用户
        UserDTO user = userService.getCurrentUser();

        if (user != null) {
            return Result.success(user);
        } else {
            return Result.error(401, "用户未认证或不存在");
        }
    }
}