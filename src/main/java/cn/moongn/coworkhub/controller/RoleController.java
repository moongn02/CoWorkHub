package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.Role;
import cn.moongn.coworkhub.model.dto.RoleDTO;
import cn.moongn.coworkhub.service.PermissionService;
import cn.moongn.coworkhub.service.RolePermissionService;
import cn.moongn.coworkhub.service.RoleService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final RolePermissionService rolePermissionService;
    private final PermissionService permissionService;

    /**
     * 分页获取角色列表
     */
    @GetMapping("/page_list")
    public Result<Page<RoleDTO>> pageRoles(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("status", status);

        Page<RoleDTO> page = roleService.pageRoles(current, size, params);
        return Result.success(page);
    }

    /**
     * 获取所有角色列表（用于下拉选择）
     */
    @GetMapping("/list")
    public Result<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return Result.success(roles);
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    public Result<RoleDTO> getRoleDetail(@PathVariable Long id) {
        RoleDTO role = roleService.getRoleDetail(id);
        if (role == null) {
            return Result.error("角色不存在");
        }
        return Result.success(role);
    }

    /**
     * 为角色分配权限
     */
    @PostMapping("/assign")
    public Result<Boolean> assignPermissions(@RequestParam Long roleId, @RequestBody List<Long> permissionIds) {
        boolean success = false;
        if (roleId == null || permissionIds != null || !permissionIds.isEmpty()) {
            List<Long> leafPermissionIds = filterLeafPermissions(permissionIds);
            success = rolePermissionService.assignPermissions(roleId, leafPermissionIds);
        }

        return success ? Result.success(true) : Result.error("权限分配失败");
    }

    /**
     * 添加角色
     */
    @PostMapping("/add")
    public Result<Boolean> addRole(@RequestBody Role role) {
        boolean success = roleService.addRole(role);

        if (success && role.getPermissionIds() != null && !role.getPermissionIds().isEmpty()) {
            // 只保存子权限，过滤掉半选中的父权限
            List<Long> leafPermissionIds = filterLeafPermissions(role.getPermissionIds());
            rolePermissionService.assignPermissions(role.getId(), leafPermissionIds);
        }

        return success ? Result.success(true) : Result.error("添加角色失败");
    }

    /**
     * 更新角色
     */
    @PutMapping("/update/{id}")
    public Result<Boolean> updateRole(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        boolean success = roleService.updateRole(role);

        if (success) {
            // 只保存子权限，过滤掉半选中的父权限
            List<Long> leafPermissionIds = filterLeafPermissions(role.getPermissionIds());
            rolePermissionService.assignPermissions(id, leafPermissionIds);
        }

        return success ? Result.success(true) : Result.error("更新角色失败");
    }

    /**
     * 更新角色状态
     */
    @PutMapping("/update_status/{id}")
    public Result<Boolean> updateRoleStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = roleService.updateRoleStatus(id, status);
        return success ? Result.success(true) : Result.error("更新角色状态失败");
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteRole(@PathVariable Long id) {
        boolean success = roleService.deleteRoleWithPermissions(id);
        return success ? Result.success(true) : Result.error("删除角色失败");
    }

    /**
     * 批量删除角色
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteRoles(@RequestBody Map<String, List<Long>> params) {
        List<Long> ids = params.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.error("未提供角色ID");
        }

        boolean success = roleService.batchDeleteRolesWithPermissions(ids);
        return success ? Result.success(true) : Result.error("批量删除角色失败");
    }

    /**
     * 过滤出叶子节点权限ID（子权限）
     * @param allPermissionIds 所有选中的权限ID（包括全选和半选）
     * @return 叶子节点权限ID
     */
    private List<Long> filterLeafPermissions(List<Long> allPermissionIds) {
        if (allPermissionIds == null || allPermissionIds.isEmpty()) {
            return List.of();
        }

        // 获取所有权限的父子关系
        return permissionService.filterLeafPermissions(allPermissionIds);
    }
}
