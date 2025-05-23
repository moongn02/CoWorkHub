package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.common.exception.ApiException;
import cn.moongn.coworkhub.mapper.RoleMapper;
import cn.moongn.coworkhub.mapper.RolePermissionMapper;
import cn.moongn.coworkhub.mapper.PermissionMapper;
import cn.moongn.coworkhub.mapper.UserMapper;
import cn.moongn.coworkhub.model.Role;
import cn.moongn.coworkhub.model.Permission;
import cn.moongn.coworkhub.model.dto.RoleDTO;
import cn.moongn.coworkhub.service.RoleService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final UserMapper userMapper;

    @Override
    public String getRoleName(Long roleId) {
        if (roleId == null || roleId == 0) {
            return "-";
        }
        Role role = roleMapper.getById(roleId);
        return role != null ? role.getName() : null;
    }

    @Override
    public Page<RoleDTO> pageRoles(int current, int size, Map<String, Object> params) {
        // 创建分页对象
        Page<Role> page = new Page<>(current, size);

        // 获取查询参数
        String keyword = params.get("keyword") != null ? params.get("keyword").toString() : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;

        // 执行分页查询
        Page<Role> rolePage = roleMapper.selectRolePage(page, keyword, status);

        // 转换为DTO
        Page<RoleDTO> dtoPage = new Page<>(rolePage.getCurrent(), rolePage.getSize(), rolePage.getTotal());
        List<RoleDTO> dtoList = rolePage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleMapper.selectAllRoles();
        return roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoleDTO getRoleDetail(Long id) {
        Role role = roleMapper.getById(id);
        if (role == null) {
            return null;
        }
        return convertToDTO(role);
    }

    @Override
    @Transactional
    public boolean addRole(Role role) {
        // 检查角色名是否已存在
        Long count = lambdaQuery().eq(Role::getName, role.getName()).count();
        if (count > 0) {
            throw new ApiException("角色名已存在");
        }

        return roleMapper.insert(role) > 0;
    }

    @Override
    @Transactional
    public boolean updateRole(Role role) {
        // 检查角色名是否已存在
        Long count = lambdaQuery()
                .eq(Role::getName, role.getName())
                .ne(Role::getId, role.getId())
                .count();
        if (count > 0) {
            throw new ApiException("角色名已存在");
        }

        return this.updateById(role);
    }

    @Override
    @Transactional
    public boolean updateRoleStatus(Long id, Integer status) {
        Role role = new Role();
        role.setId(id);
        role.setStatus(status);
        return this.updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRoleWithPermissions(Long roleId) {
        // 1. 检查角色是否被用户使用
        if (isRoleUsedByUsers(roleId)) {
            throw new ApiException("该角色已被用户使用，不能删除");
        }

        // 2. 删除角色权限关联
        rolePermissionMapper.deleteByRoleId(roleId);

        // 3. 删除角色
        return roleMapper.deleteById(roleId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteRolesWithPermissions(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return false;
        }

        // 1. 检查角色是否被用户使用
        for (Long roleId : roleIds) {
            if (isRoleUsedByUsers(roleId)) {
                throw new IllegalStateException("角色ID为" + roleId + "的角色已被用户使用，不能删除");
            }
        }

        // 2. 批量删除角色权限关联
        for (Long roleId : roleIds) {
            rolePermissionMapper.deleteByRoleId(roleId);
        }

        // 3. 批量删除角色
        return roleMapper.deleteBatchIds(roleIds) > 0;
    }

    @Override
    public boolean isRoleUsedByUsers(Long roleId) {
        // 查询是否有用户使用此角色
        Integer count = userMapper.countUsersByRoleId(roleId);
        return count != null && count > 0;
    }

    /**
     * 将Role实体转换为RoleDTO
     */
    private RoleDTO convertToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        BeanUtils.copyProperties(role, dto);

        // 设置状态文本
        dto.setStatusText(role.getStatus() == 1 ? "启用" : "禁用");

        // 获取角色拥有的权限ID列表
        List<Long> permissionIds = rolePermissionMapper.getPermissionIdsByRoleId(role.getId());
        dto.setPermissionIds(permissionIds);

        // 获取权限名称列表
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<Permission> permissions = permissionMapper.selectBatchIds(permissionIds);
            List<String> permissionNames = permissions.stream()
                    .map(Permission::getName)
                    .collect(Collectors.toList());
            dto.setPermissionsText(permissionNames);
        } else {
            dto.setPermissionsText(new ArrayList<>());
        }

        return dto;
    }
}
