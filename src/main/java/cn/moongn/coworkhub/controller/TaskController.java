package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.Task;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.TaskDTO;
import cn.moongn.coworkhub.service.TaskService;
import cn.moongn.coworkhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
            TaskDTO taskDTO = taskService.convertToDTO(task);
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
}