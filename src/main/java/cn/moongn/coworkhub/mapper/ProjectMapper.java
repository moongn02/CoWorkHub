package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.Project;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {

    /**
     * 根据ID查询项目
     * @param id 项目ID
     * @return 项目信息
     */
    Project getById(@Param("id") Long id);

    /**
     * 分页查询项目
     * @param page 分页参数
     * @param keyword 关键字
     * @param status 状态
     * @param departmentId 部门ID
     * @param parentId 父级项目ID
     * @return 分页结果
     */
    Page<Project> selectProjectPage(
            Page<Project> page,
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("departmentId") Long departmentId,
            @Param("parentId") Long parentId
    );
}
