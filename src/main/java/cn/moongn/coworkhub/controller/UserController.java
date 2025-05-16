package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.UserDTO;
import cn.moongn.coworkhub.model.vo.ResetPasswordVO;
import cn.moongn.coworkhub.model.vo.UpdateUserVO;
import cn.moongn.coworkhub.service.PermissionService;
import cn.moongn.coworkhub.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final PermissionService  permissionService;

    /**
     * 刷新当前用户权限
     */
    @GetMapping("/refresh_permissions")
    public Result<List<String>> refreshPermissions() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "用户未认证或不存在");
        }

        // 获取最新的权限列表
        List<String> permissions = permissionService.getUserPermissionCodes(currentUser.getId());
        return Result.success(permissions);
    }

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

        userService.updateById(user);

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

    /**
     * 分页获取用户列表
     */
    @GetMapping("/page_list")
    public Result<Page<UserDTO>> pageUsers(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer roleId) {

        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("status", status);
        params.put("deptId", deptId);
        params.put("roleId", roleId);

        Page<UserDTO> page = userService.pageUsers(current, size, params);
        return Result.success(page);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<UserDTO> getUserDetail(@PathVariable Long id) {
        UserDTO user = userService.getUserDetail(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 添加用户
     */
    @PostMapping("/add")
    public Result<Boolean> addUser(@RequestBody User user) {
        boolean success = userService.addUser(user);
        return success ? Result.success(true) : Result.error("添加用户失败");
    }

    /**
     * 更新用户
     */
    @PutMapping("/update/{id}")
    public Result<Boolean> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        boolean success = userService.updateUser(user);
        return success ? Result.success(true) : Result.error("更新用户失败");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        boolean success = userService.deleteUser(id);
        return success ? Result.success(true) : Result.error("删除用户失败");
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteUsers(@RequestBody Map<String, List<Long>> params) {
        List<Long> ids = params.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要删除的用户");
        }
        boolean success = userService.batchDeleteUsers(ids);
        return success ? Result.success(true) : Result.error("批量删除用户失败");
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/reset_password/{id}")
    public Result<Boolean> resetUserPassword(@PathVariable Long id) {
        boolean success = userService.resetUserPassword(id);
        return success ? Result.success(true) : Result.error("重置密码失败");
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/update_status/{id}")
    public Result<Boolean> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = userService.updateUserStatus(id, status);
        return success ? Result.success(true) : Result.error("更新用户状态失败");
    }

    /**
     * 更新用户角色
     */
    @PutMapping("/update_role/{userId}")
    public Result<Boolean> updateUserRole(@PathVariable Long userId, @RequestParam Long roleId) {
        boolean success = userService.updateUserRole(userId, roleId);
        return success ? Result.success(true) : Result.error("更新用户角色失败");
    }
}
