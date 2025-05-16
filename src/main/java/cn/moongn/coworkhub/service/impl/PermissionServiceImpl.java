package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.common.exception.ApiException;
import cn.moongn.coworkhub.mapper.PermissionMapper;
import cn.moongn.coworkhub.mapper.RolePermissionMapper;
import cn.moongn.coworkhub.mapper.UserMapper;
import cn.moongn.coworkhub.model.Permission;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.PermissionDTO;
import cn.moongn.coworkhub.service.PermissionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserMapper userMapper;

    @Override
    public Page<PermissionDTO> pagePermissions(int current, int size, Map<String, Object> params) {
        // 创建分页对象
        Page<Permission> page = new Page<>(current, size);

        // 获取查询参数
        String keyword = params.get("keyword") != null ? params.get("keyword").toString() : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        Integer type = params.get("type") != null ? Integer.parseInt(params.get("type").toString()) : null;
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
        // 检查权限编码是否已存在
        Long count = lambdaQuery().eq(Permission::getCode, permission.getCode()).count();
        if (count > 0) {
            throw new ApiException("权限编码已存在");
        }

        return this.save(permission);
    }

    @Override
    @Transactional
    public boolean updatePermission(Permission permission) {
        // 检查权限编码是否已存在
        Long count = lambdaQuery()
                .eq(Permission::getCode, permission.getCode())
                .ne(Permission::getId, permission.getId())
                .count();
        if (count > 0) {
            throw new ApiException("权限编码已存在");
        }

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
        long childCount = permissionMapper.selectCount(
                new QueryWrapper<Permission>().eq("parent_id", id)
        );
        if (childCount > 0) {
            throw new ApiException("该权限下存在子权限，无法删除");
        }

        // 检查权限是否被角色使用
        int roleCount = rolePermissionMapper.countByPermissionId(id);
        if (roleCount > 0) {
            throw new ApiException("该权限已被角色使用，无法删除");
        }

        return permissionMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean batchDeletePermissions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        // 检查是否有子权限
        for (Long id : ids) {
            Long childCount = permissionMapper.countByParentId(id);
            if (childCount > 0) {
                throw new ApiException("存在子权限，无法删除");
            }
        }

        // 检查权限是否被角色使用
        for (Long id : ids) {
            int roleCount = rolePermissionMapper.countByPermissionId(id);
            if (roleCount > 0) {
                throw new ApiException("权限已经与角色绑定，不可删除");
            }
        }

        return removeBatchByIds(ids);
    }

    @Override
    public List<PermissionDTO> getParentPermissions() {
        List<Permission> permissions = permissionMapper.selectParentPermissions();
        return permissions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Permission> getPermissionTree() {
        // 1. 获取所有状态为可用的权限
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getStatus, 1); // 假设 1 表示可用状态
        List<Permission> allPermissions = this.list(wrapper);

        // 2. 构建树形结构
        return buildPermissionTree(allPermissions);
    }

    /**
     * 构建权限树
     * @param allPermissions 所有权限列表
     * @return 树形结构的权限列表
     */
    private List<Permission> buildPermissionTree(List<Permission> allPermissions) {
        // 创建一个Map用于存储所有权限，key为权限id
        Map<Long, Permission> permissionMap = new HashMap<>();
        // 创建根节点列表
        List<Permission> rootList = new ArrayList<>();

        // 将所有权限放入Map中
        for (Permission permission : allPermissions) {
            permissionMap.put(permission.getId(), permission);
        }

        // 构建树形结构
        for (Permission permission : allPermissions) {
            Long parentId = permission.getParentId();
            if (parentId == null || parentId == 0) {
                // 如果是根节点，直接添加到根节点列表
                rootList.add(permission);
            } else {
                // 如果不是根节点，找到其父节点并添加到父节点的children中
                Permission parentPermission = permissionMap.get(parentId);
                if (parentPermission != null) {
                    if (parentPermission.getChildren() == null) {
                        parentPermission.setChildren(new ArrayList<>());
                    }
                    parentPermission.getChildren().add(permission);
                }
            }
        }

        return rootList;
    }

    @Override
    public List<Permission> getPermissionTreeByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 获取所有指定ID的权限
        List<Permission> permissions = this.listByIds(ids);
        if (permissions.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. A. 创建一个集合用于保存所有需要的权限ID（包括父权限）
        Set<Long> allNeededIds = new HashSet<>(ids);

        // 2. B. 添加所有父权限ID
        for (Permission permission : permissions) {
            Long parentId = permission.getParentId();
            // 递归查找所有父权限
            while (parentId != null && parentId > 0) {
                allNeededIds.add(parentId);
                Permission parent = this.getById(parentId);
                if (parent != null) {
                    parentId = parent.getParentId();
                } else {
                    break;
                }
            }
        }

        // 3. 获取所有需要的权限
        List<Permission> allPermissions = this.listByIds(allNeededIds);

        // 4. 构建树形结构
        return buildPermissionTree(allPermissions);
    }

    @Override
    public List<Long> filterLeafPermissions(List<Long> allPermissionIds) {
        if (allPermissionIds == null || allPermissionIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有权限，检查哪些是父权限
        List<Permission> permissions = permissionMapper.selectBatchIds(allPermissionIds);

        // 获取所有选中权限的父权限ID
        Set<Long> parentIds = new HashSet<>();
        for (Permission permission : permissions) {
            // 如果这个权限有子权限，将其ID添加到父权限集合
            Long count = permissionMapper.countByParentId(permission.getId());
            if (count > 0) {
                parentIds.add(permission.getId());
            }
        }

        // 过滤掉父权限，只保留子权限（叶子节点）
        return allPermissionIds.stream()
                .filter(id -> !parentIds.contains(id))
                .collect(Collectors.toList());
    }

    @Override
    public List<Permission> getPermissionsByRoleId(Long roleId) {
        if (roleId == null) {
            return Collections.emptyList();
        }

        // 获取角色拥有的所有权限
        List<Permission> permissions = permissionMapper.selectByRoleId(roleId);

        // 过滤出启用状态的权限
        return permissions.stream()
                .filter(p -> p.getStatus() == 1) // 1表示启用状态
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        if (userId == null || permissionCode == null || permissionCode.isEmpty()) {
            return false;
        }

        // 获取用户
        User user = userMapper.getById(userId);
        if (user == null || user.getRoleId() == null || user.getStatus() != 1) {
            return false;
        }

        // 获取角色权限列表
        List<Permission> permissions = getPermissionsByRoleId(user.getRoleId());

        // 检查是否包含指定权限
        return permissions.stream()
                .anyMatch(p -> permissionCode.equals(p.getCode()));
    }

    @Override
    public List<String> getUserPermissionCodes(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        // 获取用户
        User user = userMapper.getById(userId);
        if (user == null || user.getRoleId() == null || user.getStatus() != 1) {
            return Collections.emptyList();
        }

        // 获取角色权限
        List<Permission> permissions = getPermissionsByRoleId(user.getRoleId());

        // 提取权限代码
        return permissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getDirectParentPermissionIds(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 创建一个集合用于保存所有权限ID（包括直接父权限）
        Set<Long> allPermissionIds = new HashSet<>(permissionIds);

        // 获取所有指定ID的权限
        List<Permission> permissions = this.listByIds(permissionIds);
        if (permissions.isEmpty()) {
            return new ArrayList<>(permissionIds);
        }

        // 添加所有直接父权限ID
        for (Permission permission : permissions) {
            if (permission.getParentId() != null && permission.getParentId() > 0) {
                allPermissionIds.add(permission.getParentId());

                // 如果父权限是二级菜单，还需要添加一级菜单
                Permission parentPermission = this.getById(permission.getParentId());
                if (parentPermission != null && parentPermission.getParentId() != null && parentPermission.getParentId() > 0) {
                    allPermissionIds.add(parentPermission.getParentId());
                }
            }
        }

        return new ArrayList<>(allPermissionIds);
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



