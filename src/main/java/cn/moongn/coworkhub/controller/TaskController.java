package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.*;
import cn.moongn.coworkhub.model.dto.TaskCommentDTO;
import cn.moongn.coworkhub.model.dto.TaskDTO;
import cn.moongn.coworkhub.service.TaskCommentService;
import cn.moongn.coworkhub.service.TaskService;
import cn.moongn.coworkhub.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final UserService userService;
    private final TaskService taskService;
    private final TaskCommentService taskCommentService;

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
     * 修改任务
     */
    @PutMapping("/update")
    public Result<Boolean> updateTask(@RequestBody Task task) {
        if (task == null || task.getId() == null) {
            return Result.error("任务ID不能为空");
        }

        try {
            // 检查任务是否存在
            Task existingTask = taskService.getById(task.getId());
            if (existingTask == null) {
                return Result.error("任务不存在");
            }

            boolean success = taskService.updateById(task);

            if (success) {
                return Result.success();
            } else {
                return Result.error("修改任务失败");
            }

        } catch (Exception e) {
            return Result.error("修改任务失败: " + e.getMessage());
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
     * 转派任务
     */
    @PutMapping("/transfer/{id}")
    public Result<Boolean> transferTask(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            Long handlerId = null;
            if (params.get("handlerId") != null) {
                handlerId = Long.valueOf(params.get("handlerId").toString());
            } else {
                return Result.error("处理人不能为空");
            }

            String comment = (String) params.get("comment");

            BigDecimal workHours = BigDecimal.ZERO; // 默认为0
            if (params.containsKey("workHours") && params.get("workHours") != null) {
                workHours = new BigDecimal(params.get("workHours").toString());
            }

            // 更新任务处理人
            Task task = taskService.getById(id);
            if (task == null) {
                return Result.error("任务不存在");
            }

            task.setHandlerId(handlerId);
            boolean success = taskService.updateById(task);

            // 添加备注和工时
            if (success && comment != null && !comment.isEmpty()) {
                TaskComment taskComment = new TaskComment();
                taskComment.setTaskId(id);
                taskComment.setContent(comment);

                User currentUser = userService.getCurrentUser();
                if (currentUser == null) {
                    return Result.error("系统错误，请联系管理员");
                }
                Long currentUserId = currentUser.getId();
                taskComment.setCreatorId(currentUserId);

                taskComment.setWorkHours(workHours);
                taskCommentService.addTaskComment(taskComment);
            }

            return Result.success(success);
        } catch (Exception e) {
            return Result.error("转派任务失败: " + e.getMessage());
        }
    }

    /**
     * 更新任务状态
     */
    @PutMapping("/update_status/{id}")
    public Result<Boolean> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            Integer status = null;
            if (params.get("status") != null) {
                status = Integer.valueOf(params.get("status").toString());
            } else {
                return Result.error("状态不能为空");
            }

            String comment = (String) params.get("comment");

            BigDecimal workHours = BigDecimal.ZERO; // 默认为0
            if (params.containsKey("workHours") && params.get("workHours") != null) {
                workHours = new BigDecimal(params.get("workHours").toString());
            }

            // 更新任务状态
            Task task = taskService.getById(id);
            if (task == null) {
                return Result.error("任务不存在");
            }

            task.setStatus(status);
            boolean success = taskService.updateById(task);

            // 添加备注和工时
            if (success && comment != null && !comment.isEmpty()) {
                TaskComment taskComment = new TaskComment();
                taskComment.setTaskId(id);
                taskComment.setContent(comment);

                User currentUser = userService.getCurrentUser();
                if (currentUser == null) {
                    return Result.error("系统错误，请联系管理员");
                }
                Long currentUserId = currentUser.getId();
                taskComment.setCreatorId(currentUserId);

                taskComment.setWorkHours(workHours);
                taskCommentService.addTaskComment(taskComment);
            }

            return Result.success(success);
        } catch (Exception e) {
            return Result.error("更新任务状态失败: " + e.getMessage());
        }
    }

    /**
     * 拆分子任务
     */
    @PostMapping("/split/{id}")
    public Result<Boolean> splitTask(@PathVariable Long id, @RequestBody List<Map<String, Object>> data) {
        try {
            List<Task> subTasks = new ArrayList<>();

            for (Map<String, Object> subTaskData : data) {
                Task subTask = new Task();

                // 设置子任务的基本属性
                if (subTaskData.get("title") != null) {
                    subTask.setTitle(subTaskData.get("title").toString());
                }

                // 设置部门ID
                if (subTaskData.get("departmentId") != null) {
                    subTask.setDepartmentId(Long.valueOf(subTaskData.get("departmentId").toString()));
                }

                // 设置处理人
                if (subTaskData.get("handlerId") != null) {
                    subTask.setHandlerId(Long.valueOf(subTaskData.get("handlerId").toString()));
                }

                // 设置期望完成时间
                if (subTaskData.get("expectedTime") != null) {
                    // 将字符串日期转换为LocalDateTime
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime dateTime = LocalDateTime.parse(subTaskData.get("expectedTime").toString(), formatter);
                    subTask.setExpectedTime(dateTime);
                }

                // 设置内容
                if (subTaskData.get("content") != null) {
                    subTask.setContent(subTaskData.get("content").toString());
                }

                User currentUser = userService.getCurrentUser();
                if (currentUser == null) {
                    return Result.error("系统错误，请联系管理员");
                }
                Long currentUserId = currentUser.getId();
                subTask.setCreatorId(currentUserId);

                subTasks.add(subTask);
            }

            boolean success = taskService.splitTask(id, subTasks);

            if (success) {
                return Result.success();
            } else {
                return Result.error("任务拆分失败");
            }
        } catch (Exception e) {
            return Result.error("任务拆分失败: " + e.getMessage());
        }
    }

    /**
     * 修改期望完成时间
     */
    @PutMapping("/expected_time/{id}")
    public Result<Boolean> updateExpectedTime(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            String expectedTime = (String) params.get("expectedTime");
            if (expectedTime != null && expectedTime.contains("/")) {
                expectedTime = expectedTime.replace("/", "-");
            }

            String comment = (String) params.get("comment");

            BigDecimal workHours = BigDecimal.ZERO; // 默认为0
            if (params.containsKey("workHours") && params.get("workHours") != null) {
                workHours = new BigDecimal(params.get("workHours").toString());
            }

            // 更新任务期望完成时间
            Task task = taskService.getById(id);
            if (task == null) {
                return Result.error("任务不存在");
            }

            // 将字符串日期转换为LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(expectedTime, formatter);
            task.setExpectedTime(dateTime);

            boolean success = taskService.updateById(task);

            // 添加备注和工时
            if (success && comment != null && !comment.isEmpty()) {
                TaskComment taskComment = new TaskComment();
                taskComment.setTaskId(id);
                taskComment.setContent(comment);

                User currentUser = userService.getCurrentUser();
                if (currentUser == null) {
                    return Result.error("系统错误，请联系管理员");
                }
                Long currentUserId = currentUser.getId();
                taskComment.setCreatorId(currentUserId);

                taskComment.setWorkHours(workHours);
                taskCommentService.addTaskComment(taskComment);
            }

            return Result.success(success);
        } catch (Exception e) {
            return Result.error("修改期望完成时间失败: " + e.getMessage());
        }
    }

    /**
     * 添加任务备注
     */
    @PostMapping("/comment/{id}")
    public Result<TaskCommentDTO> addTaskComment(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            String content = (String) params.get("content");
            if (content == null || content.isEmpty()) {
                return Result.error("备注内容不能为空");
            }

            BigDecimal workHours = BigDecimal.ZERO; // 默认为0
            if (params.containsKey("workHours") && params.get("workHours") != null) {
                workHours = new BigDecimal(params.get("workHours").toString());
            }

            // 创建任务备注
            TaskComment taskComment = new TaskComment();
            taskComment.setTaskId(id);
            taskComment.setContent(content);

            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return Result.error("系统错误，请联系管理员");
            }
            Long currentUserId = currentUser.getId();
            taskComment.setCreatorId(currentUserId);

            taskComment.setWorkHours(workHours);

            boolean success = taskCommentService.addTaskComment(taskComment);
            if (success) {
                TaskCommentDTO dto = taskCommentService.convertToDTO(taskComment);
                return Result.success(dto);
            } else {
                return Result.error("添加备注失败");
            }
        } catch (Exception e) {
            return Result.error("添加任务备注失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务备注列表
     */
    @GetMapping("/comments/{id}")
    public Result<List<TaskCommentDTO>> getTaskComments(@PathVariable Long id) {
        List<TaskCommentDTO> comments = taskCommentService.getTaskComments(id);
        return Result.success(comments);
    }

    /**
     * 辅助方法：获取状态文本
     */
    private String getStatusText(Integer status) {
        return switch (status) {
            case 1 -> "已分派";
            case 2 -> "处理中";
            case 3 -> "已解决";
            case 4 -> "已暂停";
            case 5 -> "已关闭";
            default -> "未知";
        };
    }

    /**
     * 获取任务的关联问题
     */
    @GetMapping("/related_issues/{id}")
    public Result<List<Map<String, Object>>> getRelatedIssues(@PathVariable Long id) {
        try {
            List<Issue> issues = taskService.getIssuesByTaskId(id);
            List<Map<String, Object>> result = new ArrayList<>();

            for (Issue issue : issues) {
                // 获取处理人信息
                String handlerName = "";
                if (issue.getHandlerId() != null) {
                    User handler = userService.getById(issue.getHandlerId());
                    if (handler != null) {
                        handlerName = handler.getRealName();
                    }
                }

                Map<String, Object> issueMap = new HashMap<>();
                issueMap.put("id", issue.getId());
                issueMap.put("title", issue.getTitle());
                issueMap.put("handlerName", handlerName);
                issueMap.put("status", issue.getStatus());
                issueMap.put("statusText", getStatusText(issue.getStatus()));
                issueMap.put("expectedTime", issue.getExpectedTime());
                result.add(issueMap);
            }

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取关联问题失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取任务备注
     */
    @GetMapping("/comments/page/{id}")
    public Result<Page<TaskCommentDTO>> pageTaskComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<TaskCommentDTO> page = taskCommentService.pageTaskComments(id, pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("分页获取任务备注失败: " + e.getMessage());
        }
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
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

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
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        Page<TaskDTO> page = taskService.pageTasks(pageNum, pageSize, params);
        return Result.success(page);
    }
}