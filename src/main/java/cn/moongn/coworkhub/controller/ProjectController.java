package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.Project;
import cn.moongn.coworkhub.model.dto.ProjectDTO;
import cn.moongn.coworkhub.service.DepartmentService;
import cn.moongn.coworkhub.service.ProjectService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final DepartmentService departmentService;

    /**
     * 分页查询项目列表
     */
    @GetMapping("/page_list")
    public Result<Page<ProjectDTO>> pageProjects(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long parentId) {

        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("status", status);
        params.put("departmentId", departmentId);
        params.put("parentId", parentId);

        Page<ProjectDTO> page = projectService.pageProjects(current, size, params);
        return Result.success(page);
    }

    /**
     * 获取项目详情
     */
    @GetMapping("/{id}")
    public Result<ProjectDTO> getProjectDetail(@PathVariable Long id) {
        ProjectDTO project = projectService.getProjectDetail(id);
        if (project == null) {
            return Result.error("项目不存在");
        }
        return Result.success(project);
    }

    /**
     * 添加项目
     */
    @PostMapping("/add")
    public Result<Boolean> addProject(@RequestBody Project project) {
        boolean success = projectService.addProject(project);
        return success ? Result.success(true) : Result.error("添加项目失败");
    }

    /**
     * 更新项目
     */
    @PutMapping("/update/{id}")
    public Result<Boolean> updateProject(@PathVariable Long id, @RequestBody Project project) {
        project.setId(id);
        boolean success = projectService.updateProject(project);
        return success ? Result.success(true) : Result.error("更新项目失败");
    }

    /**
     * 更新项目状态
     */
    @PutMapping("/update_status/{id}")
    public Result<Boolean> updateProjectStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = projectService.updateProjectStatus(id, status);
        return success ? Result.success(true) : Result.error("更新项目状态失败");
    }

    /**
     * 获取父级项目列表（用于下拉选择）
     */
    @GetMapping("/parents")
    public Result<List<ProjectDTO>> getParentProjects() {
        List<ProjectDTO> projects = projectService.getParentProjects();
        return Result.success(projects);
    }
}
