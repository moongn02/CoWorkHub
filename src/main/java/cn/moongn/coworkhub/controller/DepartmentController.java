package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.Department;
import cn.moongn.coworkhub.model.dto.DepartmentDTO;
import cn.moongn.coworkhub.model.dto.DepartmentTreeDTO;
import cn.moongn.coworkhub.service.DepartmentService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 分页获取部门列表
     */
    @GetMapping("/page_list")
    public Result<Page<DepartmentDTO>> pageDepartments(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long parentId) {

        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("status", status);
        params.put("parentId", parentId);

        Page<DepartmentDTO> page = departmentService.pageDepartments(current, size, params);
        return Result.success(page);
    }

    /**
     * 获取所有部门列表（用于下拉选择）
     */
    @GetMapping("/list")
    public Result<List<DepartmentDTO>> getAllDepartments() {
        List<DepartmentDTO> departments = departmentService.getAllDepartments();
        return Result.success(departments);
    }

    /**
     * 获取所有一级部门（用于上级部门下拉框）
     */
    @GetMapping("/parents")
    public Result<List<DepartmentDTO>> getParentDepartments() {
        List<DepartmentDTO> departments = departmentService.getParentDepartments();
        return Result.success(departments);
    }

    /**
     * 获取部门详情
     */
    @GetMapping("/{id}")
    public Result<DepartmentDTO> getDepartmentDetail(@PathVariable Long id) {
        DepartmentDTO department = departmentService.getDepartmentDetail(id);
        if (department == null) {
            return Result.error("部门不存在");
        }
        return Result.success(department);
    }

    /**
     * 添加部门
     */
    @PostMapping("/add")
    public Result<Boolean> addDepartment(@RequestBody Department department) {
        boolean success = departmentService.addDepartment(department);
        return success ? Result.success(true) : Result.error("添加部门失败");
    }

    /**
     * 更新部门
     */
    @PutMapping("/update/{id}")
    public Result<Boolean> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        department.setId(id);
        boolean success = departmentService.updateDepartment(department);
        return success ? Result.success(true) : Result.error("更新部门失败");
    }

    /**
     * 更新部门状态
     */
    @PutMapping("/update_status/{id}")
    public Result<Boolean> updateDepartmentStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = departmentService.updateDepartmentStatus(id, status);
        return success ? Result.success(true) : Result.error("更新部门状态失败");
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteDepartment(@PathVariable Long id) {
        boolean success = departmentService.deleteDepartment(id);
        return success ? Result.success(true) : Result.error("删除部门失败");
    }

    /**
     * 批量删除部门
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteDepartments(@RequestBody Map<String, List<Long>> requestMap) {
        List<Long> ids = requestMap.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.error("未提供部门ID");
        }
        boolean success = departmentService.batchDeleteDepartments(ids);
        return success ? Result.success(true) : Result.error("批量删除失败");
    }

    /**
     * 获取部门树形结构（用于级联选择器）
     */
    @GetMapping("/tree")
    public Result<List<DepartmentTreeDTO>> getDepartmentTree() {
        List<DepartmentTreeDTO> departmentTree = departmentService.getDepartmentTree();
        return Result.success(departmentTree);
    }
}
