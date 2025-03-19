package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.Department;

import java.util.List;

public interface DepartmentService {
     String getDepartmentName(Long deptId);
     String getSupervisorName(Long deptId);
     List<Department> getList();
}
