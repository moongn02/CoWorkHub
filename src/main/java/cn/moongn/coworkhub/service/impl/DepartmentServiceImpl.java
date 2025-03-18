package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.mapper.DepartmentMapper;
import cn.moongn.coworkhub.mapper.UserMapper;
import cn.moongn.coworkhub.model.Department;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final UserMapper userMapper;

    public String getDepartmentName(Long deptId) {
        Department department = departmentMapper.getById(deptId);

        return department.getName();
    }

    public String getsupervisorName(Long deptId) {
        Department department = departmentMapper.getById(deptId);
        Long superiorId = department.getLeaderId();
        String superiorRealName = null;
        if (superiorId != null) {
            User superior = userMapper.getById(superiorId);
            superiorRealName = superior.getRealName();
        }

        return superiorRealName;
    }
}
