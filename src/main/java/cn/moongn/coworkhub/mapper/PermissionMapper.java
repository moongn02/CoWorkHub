package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.Permission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}
