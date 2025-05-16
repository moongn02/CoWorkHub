package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.Permission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 分页查询权限
     * @param page 分页对象
     * @param keyword 关键字
     * @param status 状态
     * @param type 类型
     * @param isSensitive 是否敏感权限
     * @return 分页结果
     */
    Page<Permission> selectPermissionPage(
            Page<Permission> page,
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("type") Integer type,
            @Param("isSensitive") Boolean isSensitive
    );

    /**
     * 根据ID获取权限详情
     * @param id 权限ID
     * @return 权限对象
     */
    Permission getById(@Param("id") Long id);

    /**
     * 根据角色ID查询权限列表
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询所有父级权限（用于下拉选择）
     * @return 父级权限列表
     */
    List<Permission> selectParentPermissions();

    /**
     * 根据父ID统计子权限数量
     * @param parentId 父权限ID
     * @return 子权限数量
     */
    @Select("SELECT COUNT(*) FROM permission WHERE parent_id = #{parentId}")
    Long countByParentId(@Param("parentId") Long parentId);

    /**
     * 根据权限ID获取其直接父级权限ID
     * @param permissionId 权限ID
     * @return 父级权限ID
     */
    @Select("SELECT parent_id FROM permission WHERE id = #{permissionId}")
    Long getParentIdByPermissionId(@Param("permissionId") Long permissionId);
}
