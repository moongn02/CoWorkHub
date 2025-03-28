package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    User getById(@Param("id") Long id);
    User getByUsername(@Param("username") String username);

    /**
     * 分页查询用户
     * @param page 分页参数
     * @param keyword 关键字（用户名、姓名、手机号）
     * @param status 状态
     * @param deptId 部门ID
     * @param roleId 角色ID
     * @return 分页用户列表
     */
    Page<User> selectUserPage(
            @Param("page") Page<User> page,
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("deptId") Long deptId,
            @Param("roleId") Integer roleId);
}