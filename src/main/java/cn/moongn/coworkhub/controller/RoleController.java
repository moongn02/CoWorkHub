package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.Role;
import cn.moongn.coworkhub.model.dto.RoleDTO;
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
     * 添加角色
     */
    @PostMapping("/add")
    public Result<Boolean> addRole(@RequestBody Role role) {
        boolean success = roleService.addRole(role);
        return success ? Result.success(true) : Result.error("添加角色失败");
    }

    /**
     * 更新角色
     */
    @PutMapping("/update/{id}")
    public Result<Boolean> updateRole(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        boolean success = roleService.updateRole(role);
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
        boolean success = roleService.removeById(id);
        return success ? Result.success(true) : Result.error("删除角色失败");
    }

    /**
     * 批量删除角色
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteRoles(@RequestBody Map<String, List<Long>> params) {
        List<Long> ids = params.get("ids");
        boolean success = roleService.removeByIds(ids);
        return success ? Result.success(true) : Result.error("批量删除角色失败");
    }
}
