package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.UserDTO;
import cn.moongn.coworkhub.model.vo.ResetPasswordVO;
import cn.moongn.coworkhub.model.vo.UpdateUserVO;
import cn.moongn.coworkhub.service.DepartmentService;
import cn.moongn.coworkhub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final DepartmentService departmentService;

    // 获取个人中心展示数据
    @GetMapping("/info")
    public Result<UserDTO> getUserInfo() {
        // 从认证上下文获取当前用户
        UserDTO user = userService.formatUser(userService.getCurrentUser());

        if (user != null) {
            return Result.success(user);
        } else {
            return Result.error(401, "用户未认证或不存在");
        }
    }

    // 获取编辑弹窗所需数据
    @GetMapping("/edit")
    public Result<User> getUserInfoForEdit() {
        // 从认证上下文获取当前用户
        User user = userService.getCurrentUser();

        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        } else {
            return Result.error(401, "用户未认证或不存在");
        }
    }

    // 编辑个人信息
    @PostMapping("/edit")
    public Result<Void> updateUser(@Valid @RequestBody UpdateUserVO updateUserVO) {
        User user = userService.getCurrentUser();
        BeanUtils.copyProperties(updateUserVO, user);

        if (updateUserVO.getBirthday() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            user.setBirthday(LocalDate.parse(sdf.format(updateUserVO.getBirthday())));
        }

        userService.update(user);

        return Result.success();
    }

    // 修改密码
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ResetPasswordVO resetPasswordVO) {
        userService.changePassword(resetPasswordVO);

        return Result.success();
    }

    // 获取用户列表
    @GetMapping("/list")
    public Result<List<UserDTO>> getUserList() {
        List<UserDTO> userList = userService.getUserList();

        if (userList != null) {
            return Result.success(userList);
        } else {
            return Result.error("获取失败，请联系管理员");
        }
    }
}