package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.mapper.PermissionMapper;
import cn.moongn.coworkhub.mapper.RolePermissionMapper;
import cn.moongn.coworkhub.model.Permission;
import cn.moongn.coworkhub.model.RolePermission;
import cn.moongn.coworkhub.service.RolePermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {

    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        // 先删除原有的权限关联
        rolePermissionMapper.deleteByRoleId(roleId);

        // 如果权限列表为空，则直接返回成功
        if (permissionIds == null || permissionIds.isEmpty()) {
            return true;
        }

        // 批量插入新的权限关联
        return rolePermissionMapper.batchInsert(roleId, permissionIds) > 0;
    }

    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        List<Permission> permissions = permissionMapper.selectByRoleId(roleId);
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }
        return permissions.stream()
                .map(Permission::getId)
                .collect(Collectors.toList());
    }
}
