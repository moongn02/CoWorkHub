package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.mapper.RoleMapper;
import cn.moongn.coworkhub.mapper.RolePermissionMapper;
import cn.moongn.coworkhub.mapper.PermissionMapper;
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
        return this.updateById(role);
    }

    @Override
    @Transactional
    public boolean updateRole(Role role) {
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
