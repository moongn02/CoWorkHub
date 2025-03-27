package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role_permission")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    /**
     * 为角色分配权限
     */
    @PostMapping("/assign")
    public Result<Boolean> assignPermissions(
            @RequestParam Long roleId,
            @RequestBody List<Long> permissionIds) {
        boolean success = rolePermissionService.assignPermissions(roleId, permissionIds);
        return success ? Result.success(true) : Result.error("权限分配失败");
    }

    /**
     * 获取角色的权限ID列表
     */
    @GetMapping("/role/{roleId}")
    public Result<List<Long>> getPermissionIdsByRoleId(@PathVariable Long roleId) {
        List<Long> permissionIds = rolePermissionService.getPermissionIdsByRoleId(roleId);
        return Result.success(permissionIds);
    }
}
