package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.RolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 批量插入角色权限关系
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 插入数量
     */
    int batchInsert(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

    /**
     * 根据角色ID删除角色权限关系
     * @param roleId 角色ID
     * @return 删除数量
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限ID统计关联的角色数量
     * @param permissionId 权限ID
     * @return 关联的角色数量
     */
    int countByPermissionId(@Param("permissionId") Long permissionId);
}
