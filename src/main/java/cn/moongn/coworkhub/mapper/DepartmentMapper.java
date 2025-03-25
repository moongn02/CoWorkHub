package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.Department;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
    /**
     * 根据ID获取部门
     */
    Department getById(@Param("id") Long id);

    /**
     * 分页查询部门列表
     */
    Page<Department> selectDepartmentPage(Page<Department> page,
                                          @Param("keyword") String keyword,
                                          @Param("status") Integer status,
                                          @Param("parentId") Long parentId);

    /**
     * 获取所有部门列表
     */
    List<Department> selectAllDepartments();

}
