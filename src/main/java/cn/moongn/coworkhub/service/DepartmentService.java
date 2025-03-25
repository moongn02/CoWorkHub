package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.Department;
import cn.moongn.coworkhub.model.dto.DepartmentDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface DepartmentService extends IService<Department> {
     /**
      * 获取部门名称
      */
     String getDepartmentName(Long deptId);

     /**
      * 获取部门负责人姓名
      */
     String getSupervisorName(Long deptId);

     /**
      * 分页查询部门列表
      */
     Page<DepartmentDTO> pageDepartments(int current, int size, Map<String, Object> params);

     /**
      * 获取所有部门列表
      */
     List<DepartmentDTO> getAllDepartments();

     /**
      * 获取所有一级部门（即所有parent_id = 0的部门）
      */
     List<DepartmentDTO> getParentDepartments();

     /**
      * 获取部门详情
      */
     DepartmentDTO getDepartmentDetail(Long id);

     /**
      * 添加部门
      */
     boolean addDepartment(Department department);

     /**
      * 更新部门
      */
     boolean updateDepartment(Department department);

     /**
      * 更新部门状态
      */
     boolean updateDepartmentStatus(Long id, Integer status);
}
