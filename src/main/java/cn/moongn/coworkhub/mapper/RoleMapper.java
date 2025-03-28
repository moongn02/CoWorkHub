package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    /**
     * 根据ID获取角色
     */
    Role getById(@Param("id") Long id);

    /**
     * 分页查询角色列表
     */
    Page<Role> selectRolePage(Page<Role> page,
                              @Param("keyword") String keyword,
                              @Param("status") Integer status);

    /**
     * 获取所有角色列表
     */
    List<Role> selectAllRoles();
}
