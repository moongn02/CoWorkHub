package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.Permission;
import cn.moongn.coworkhub.model.dto.PermissionDTO;
import cn.moongn.coworkhub.service.PermissionService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 分页查询权限列表
     */
    @GetMapping("/page_list")
    public Result<Page<PermissionDTO>> pagePermissions(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Boolean isSensitive) {

        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("status", status);
        params.put("type", type);
        params.put("isSensitive", isSensitive);

        Page<PermissionDTO> page = permissionService.pagePermissions(current, size, params);
        return Result.success(page);
    }

    /**
     * 获取权限详情
     */
    @GetMapping("/{id}")
    public Result<PermissionDTO> getPermission(@PathVariable Long id) {
        PermissionDTO permission = permissionService.getPermissionById(id);
        if (permission == null) {
            return Result.error("权限不存在");
        }
        return Result.success(permission);
    }

    /**
     * 添加权限
     */
    @PostMapping("/add")
    public Result<Boolean> addPermission(@RequestBody @Validated Permission permission) {
        boolean success = permissionService.addPermission(permission);
        return success ? Result.success(true) : Result.error("添加失败");
    }

    /**
     * 更新权限
     */
    @PutMapping("/update/{id}")
    public Result<Boolean> updatePermission(@PathVariable Long id, @RequestBody Permission permission) {
        permission.setId(id);
        boolean success = permissionService.updatePermission(permission);
        return success ? Result.success(true) : Result.error("更新失败或权限不存在");
    }

    /**
     * 更新权限状态
     */
    @PutMapping("/update_status/{id}")
    public Result<Boolean> updatePermissionStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status != 0 && status != 1) {
            return Result.error("状态值无效");
        }
        boolean success = permissionService.updatePermissionStatus(id, status);
        return success ? Result.success(true) : Result.error("状态更新失败或权限不存在");
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deletePermission(@PathVariable Long id) {
        boolean success = permissionService.deletePermission(id);
        return success ? Result.success(true) : Result.error("删除权限失败");
    }

    /**
     * 批量删除权限
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeletePermissions(@RequestBody Map<String, List<Long>> requestMap) {
        List<Long> ids = requestMap.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.error("未提供权限ID");
        }
        boolean success = permissionService.batchDeletePermissions(ids);
        return success ? Result.success(true) : Result.error("批量删除失败");
    }

    /**
     * 获取一级权限列表（用于下拉选择）
     */
    @GetMapping("/parents")
    public Result<List<PermissionDTO>> getParentPermissions() {
        List<PermissionDTO> permissions = permissionService.getParentPermissions();
        return Result.success(permissions);
    }

    /**
     * 获取权限树
     */
    @GetMapping("/tree")
    public Result<List<Permission>> getPermissionTree() {
        List<Permission> tree = permissionService.getPermissionTree();
        return Result.success(tree);
    }

    /**
     * 根据ID列表获取权限树
     */
    @PostMapping("/tree_by_ids")
    public Result<List<Permission>> getPermissionTreeByIds(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("获取错误");
        }
        List<Permission> tree = permissionService.getPermissionTreeByIds(ids);
        return Result.success(tree);
    }
}
