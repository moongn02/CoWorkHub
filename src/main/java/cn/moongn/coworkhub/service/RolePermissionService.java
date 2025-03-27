package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.RolePermission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface RolePermissionService extends IService<RolePermission> {

    /**
     * 为角色分配权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 是否成功
     */
    boolean assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色的权限ID列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> getPermissionIdsByRoleId(Long roleId);
}
