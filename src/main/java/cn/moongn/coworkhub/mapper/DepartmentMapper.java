package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.Department;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
//    int insert(Department department);
//    int update(Department department);
    Department getById(Long id);
    List<Department> getList();
//    int deleteById(Long id);
}
