package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.dto.UserDTO;
import cn.moongn.coworkhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
//    @GetMapping("/info")
//    public Result<UserDTO> getUserInfo(@RequestParam String username) {
//        return Result.success(userService.getUserInfo(username));
//    }
    
    @GetMapping("/info")
    public Result<UserDTO> getUserById(@RequestParam Long id) {
        return Result.success(userService.getById(id));
    }
}