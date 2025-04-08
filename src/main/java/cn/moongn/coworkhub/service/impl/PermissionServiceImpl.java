package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.mapper.PermissionMapper;
import cn.moongn.coworkhub.mapper.RolePermissionMapper;
import cn.moongn.coworkhub.model.Permission;
import cn.moongn.coworkhub.model.dto.PermissionDTO;
import cn.moongn.coworkhub.service.PermissionService;
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
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public Page<PermissionDTO> pagePermissions(int current, int size, Map<String, Object> params) {
        // 创建分页对象
        Page<Permission> page = new Page<>(current, size);

        // 获取查询参数
        String keyword = params.get("keyword") != null ? params.get("keyword").toString() : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        Integer type = params.get("departmentId") != null ? Integer.parseInt(params.get("departmentId").toString()) : null;
        Boolean isSensitive = params.get("isSensitive") != null ? Boolean.parseBoolean(params.get("isSensitive").toString()) : null;

        // 执行分页查询
        Page<Permission> permissionPage = permissionMapper.selectPermissionPage(page, keyword, status, type, isSensitive);

        // 转换为DTO
        Page<PermissionDTO> dtoPage = new Page<>(permissionPage.getCurrent(), permissionPage.getSize(), permissionPage.getTotal());

        if (permissionPage.getRecords().isEmpty()) {
            dtoPage.setRecords(new ArrayList<>());
            return dtoPage;
        }

        // 转换为DTO
        List<PermissionDTO> dtoList = permissionPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public PermissionDTO getPermissionById(Long id) {
        Permission permission = permissionMapper.getById(id);
        if (permission == null) {
            return null;
        }
        return convertToDTO(permission);
    }

    @Override
    @Transactional
    public boolean addPermission(Permission permission) {

        return this.updateById(permission);
    }

    @Override
    @Transactional
    public boolean updatePermission(Permission permission) {

        return this.updateById(permission);
    }

    @Override
    @Transactional
    public boolean updatePermissionStatus(Long id, Integer status) {
        Permission permission = permissionMapper.getById(id);
        if (permission == null) {
            return false;
        }

        permission.setStatus(status);

        return permissionMapper.updateById(permission) > 0;
    }

    @Override
    @Transactional
    public boolean deletePermission(Long id) {
        // 检查权限是否存在
        Permission permission = permissionMapper.getById(id);
        if (permission == null) {
            return false;
        }

        // 检查是否有子权限
        long childCount = count(lambdaQuery().eq(Permission::getParentId, id));
        if (childCount > 0) {
            throw new RuntimeException("该权限下存在子权限，无法删除");
        }

        // 检查权限是否被角色使用
        int roleCount = rolePermissionMapper.countByPermissionId(id);
        if (roleCount > 0) {
            throw new RuntimeException("该权限已被角色使用，无法删除");
        }

        return permissionMapper.deleteById(id) > 0;
    }

    @Override
    public List<PermissionDTO> getParentPermissions() {
        List<Permission> permissions = permissionMapper.selectParentPermissions();
        return permissions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 将Permission实体转换为DTO
     * @param permission 权限实体
     * @return 权限DTO
     */
    private PermissionDTO convertToDTO(Permission permission) {
        if (permission == null) {
            return null;
        }

        PermissionDTO dto = new PermissionDTO();
        BeanUtils.copyProperties(permission, dto);

        // 设置状态文本
        dto.setStatusText(permission.getStatus() == 1 ? "启用" : "禁用");

        // 设置类型文本
        dto.setTypeText(permission.getType() == 1 ? "菜单权限" : "按钮权限");

        // 如果有上级权限，获取上级权限名称
        if (permission.getParentId() != null && permission.getParentId() > 0) {
            Permission parentPermission = permissionMapper.getById(permission.getParentId());
            if (parentPermission != null) {
                dto.setParentName(parentPermission.getName());
            }
        } else {
            dto.setParentName("-");
        }

        return dto;
    }
}



