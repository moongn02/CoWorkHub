package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.Department;
import cn.moongn.coworkhub.model.dto.DepartmentDTO;
import cn.moongn.coworkhub.model.dto.DepartmentTreeDTO;
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
      * 获取部门树形结构（用于级联选择器）
      */
     List<DepartmentTreeDTO> getDepartmentTree();

     /**
      * 添加部门
      */
     boolean addDepartment(Department department);

     /**
      * 更新部门
      */
     boolean updateDepartment(Department department);

     /**
      * 删除部门
      * @param id 部门ID
      * @return 是否成功
      */
     boolean deleteDepartment(Long id);

     /**
      * 更新部门状态
      */
     boolean updateDepartmentStatus(Long id, Integer status);

     /**
      * 批量删除部门
      * @param ids 部门ID列表
      * @return 是否成功
      */
     boolean batchDeleteDepartments(List<Long> ids);
}
