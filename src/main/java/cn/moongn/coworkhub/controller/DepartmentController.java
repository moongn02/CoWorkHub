package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.Department;
import cn.moongn.coworkhub.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/list")
    public Result<List<Department>> getDepartmentList() {
        List<Department> departmentList = departmentService.getList();
        if (departmentList == null) {
            return Result.error("部门数据获取失败，请重试");
        }

        return Result.success(departmentList);
    }


}
