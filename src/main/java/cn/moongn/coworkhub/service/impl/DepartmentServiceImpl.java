package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.service.DepartmentService;

public class DepartmentServiceImpl implements DepartmentService {

    public String getDepartmentName(Long deptId) {
        // 实现获取部门名称的逻辑
        return "部门名称"; // 示例
    }


    public String getSuperiorName(Long deptId) {
        // 实现获取上级名称的逻辑
        return "上级名称"; // 示例
    }
}
