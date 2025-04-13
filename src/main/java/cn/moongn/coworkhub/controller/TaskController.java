package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.Task;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.TaskDTO;
import cn.moongn.coworkhub.service.TaskService;
import cn.moongn.coworkhub.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final UserService userService;
    private final TaskService taskService;

    /**
     * 创建任务
     */
    @PostMapping("/create")
    public Result<TaskDTO> createTask(@RequestBody Task task) {
        // 设置创建者ID为当前登录用户
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return Result.error("系统错误，请联系管理员");
        }
        Long currentUserId = currentUser.getId();
        task.setCreatorId(currentUserId);

        boolean success = taskService.createTask(task);

        if (success) {
            Task savedTask = taskService.getById(task.getId());
            TaskDTO taskDTO = taskService.convertToDTO(savedTask);
            return Result.success(taskDTO);
        } else {
            return Result.error("创建任务失败");
        }
    }


    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    public Result<TaskDTO> getTaskDetail(@PathVariable Long id) {
        Task task = taskService.getById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }

        TaskDTO taskDTO = taskService.convertToDTO(task);
        return Result.success(taskDTO);
    }

    /**
     * 分页获取任务列表
     */
    @GetMapping("/page_list")
    public Result<Page<TaskDTO>> pageTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) Long handlerId,
            @RequestParam(required = false) Long acceptorId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long parentTaskId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer priority) {

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("title", title);
        params.put("creatorId", creatorId);
        params.put("handlerId", handlerId);
        params.put("acceptorId", acceptorId);
        params.put("projectId", projectId);
        params.put("departmentId", departmentId);
        params.put("parentTaskId", parentTaskId);
        params.put("status", status);
        params.put("priority", priority);

        Page<TaskDTO> page = taskService.pageTasks(pageNum, pageSize, params);
        return Result.success(page);
    }
}