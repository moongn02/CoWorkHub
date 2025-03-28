package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.Role;
import cn.moongn.coworkhub.model.dto.RoleDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface RoleService extends IService<Role> {
    /**
     * 分页查询角色列表
     */
    Page<RoleDTO> pageRoles(int current, int size, Map<String, Object> params);

    /**
     * 获取所有角色列表
     */
    List<RoleDTO> getAllRoles();

    /**
     * 获取角色详情
     */
    RoleDTO getRoleDetail(Long id);

    /**
     * 添加角色
     */
    boolean addRole(Role role);

    /**
     * 更新角色
     */
    boolean updateRole(Role role);

    /**
     * 更新角色状态
     */
    boolean updateRoleStatus(Long id, Integer status);

    /**
     * 获取角色名称
     */
    String getRoleName(Long roleId);
}
