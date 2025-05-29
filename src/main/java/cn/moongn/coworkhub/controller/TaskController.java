package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.common.utils.EmailUtils;
import cn.moongn.coworkhub.common.utils.TaskActivityRecorder;
import cn.moongn.coworkhub.common.constant.enums.TaskActivityType;
import cn.moongn.coworkhub.model.*;
import cn.moongn.coworkhub.model.dto.TaskActivityDTO;
import cn.moongn.coworkhub.model.dto.TaskCommentDTO;
import cn.moongn.coworkhub.model.dto.TaskDTO;
import cn.moongn.coworkhub.service.TaskActivityService;
import cn.moongn.coworkhub.service.TaskCommentService;
import cn.moongn.coworkhub.service.TaskService;
import cn.moongn.coworkhub.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final UserService userService;
    private final TaskService taskService;
    private final TaskCommentService taskCommentService;
    private final TaskActivityService taskActivityService;
    private final TaskActivityRecorder taskActivityRecorder;
    private final EmailUtils emailUtils;

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
            taskActivityRecorder.record(task.getId(), TaskActivityType.CREATE);

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

            task.setUpdateTime(LocalDateTime.now());

            // 检查状态是否变更
            if (task.getStatus() != null && !task.getStatus().equals(existingTask.getStatus())) {
                task.setLastStatusChangedTime(LocalDateTime.now());
            }

            // 检查处理人是否变更
            if (task.getHandlerId() != null && !task.getHandlerId().equals(existingTask.getHandlerId())) {
                task.setLastAssignedTime(LocalDateTime.now());
            }

            boolean success = taskService.updateById(task);

            if (success) {
                taskActivityRecorder.record(task.getId(), TaskActivityType.UPDATE);

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

            // 检查处理人是否变更
            if (!handlerId.equals(task.getHandlerId())) {
                task.setLastAssignedTime(LocalDateTime.now());
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

            if (success) {
                User handler = userService.getById(handlerId);
                taskActivityRecorder.record(id, TaskActivityType.TRANSFER, handler.getRealName());
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
    public Result<?> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            Integer status = params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : null;
            if (status == null) return Result.error("状态不能为空");

            String comment = (String) params.get("comment");
            boolean forceComplete = params.get("forceComplete") != null && Boolean.parseBoolean(params.get("forceComplete").toString());

            BigDecimal workHours = BigDecimal.ZERO; // 默认为0
            if (params.containsKey("workHours") && params.get("workHours") != null) {
                workHours = new BigDecimal(params.get("workHours").toString());
            }

            // 更新任务状态
            Task task = taskService.getById(id);
            if (task == null) return Result.error("任务不存在");

            // 已完成时检查前置/后置任务情况并发送开始提醒邮件
            if (status == 3) {
                // 获取前置任务和后置任务
                List<Task> preTasks = taskService.getPreTasks(id);
                List<Task> postTasks = taskService.getPostTasks(id);

                boolean allPreFinished = true;
                if (preTasks != null && !preTasks.isEmpty()) {
                    for (Task preTask : preTasks) {
                        if (preTask.getStatus() == null || preTask.getStatus() != 3) {
                            allPreFinished = false;
                            break;
                        }
                    }
                }

                // 如果有前置任务且全部已完成，并且有后置任务
                if (allPreFinished && postTasks != null && !postTasks.isEmpty()) {
                    for (Task postTask : postTasks) {
                        if (postTask.getHandlerId() != null) {
                            User handler = userService.getById(postTask.getHandlerId());
                            if (handler != null && handler.getEmail() != null && !handler.getEmail().isEmpty()) {
                                // 发送邮件通知
                                emailUtils.sendPostTaskNotifyMail(
                                        handler.getEmail(),
                                        handler.getRealName(),
                                        postTask.getTitle(),
                                        postTask.getId(),
                                        postTask.getExpectedTime() != null ? postTask.getExpectedTime().toString() : ""
                                );
                            }
                        }
                    }
                }
            }

            // 只在要改为已完成时校验
            if (status == 3) {
                List<Task> subTasks = taskService.getSubTasks(id);
                List<Long> unfinishedSubTaskIds = new ArrayList<>();
                List<Long> subTaskHasSubTaskIds = new ArrayList<>();

                for (Task subTask : subTasks) {
                    // 检查子任务下是否还有子任务
                    List<Task> subSubTasks = taskService.getSubTasks(subTask.getId());
                    if (subSubTasks != null && !subSubTasks.isEmpty()) {
                        subTaskHasSubTaskIds.add(subTask.getId());
                    }
                    // 记录未完成的子任务
                    if (subTask.getStatus() != 3) {
                        unfinishedSubTaskIds.add(subTask.getId());
                    }
                }

                // 有未完成子任务，且未强制完成，提示
                if (!unfinishedSubTaskIds.isEmpty() && !forceComplete) {
                    return Result.error("存在未完成的子任务" + unfinishedSubTaskIds, unfinishedSubTaskIds);
                }

                // 若有未完成子任务，且强制完成，则批量更新所有子任务为已完成
                if (!unfinishedSubTaskIds.isEmpty() && forceComplete) {
                    // 有子任务下还有子任务，报错
                    if (!subTaskHasSubTaskIds.isEmpty()) {
                        String ids = subTaskHasSubTaskIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
                        return Result.error("子任务" + ids + "下存在子任务，无法同步变更状态");
                    }

                    for (Long subTaskId : unfinishedSubTaskIds) {
                        Task subTask = taskService.getById(subTaskId);
                        if (subTask != null) {
                            subTask.setStatus(3);
                            subTask.setLastStatusChangedTime(LocalDateTime.now());
                            taskService.updateById(subTask);

                            String statusText = getTaskStatusText(status);
                            taskActivityRecorder.record(subTaskId, TaskActivityType.SYNC_CHANGE_STATUS, statusText);
                        }
                    }
                }
            }

            // 状态未变更时无需更新
            if (status.equals(task.getStatus())) {
                return Result.success();
            }

            task.setStatus(status);
            task.setLastStatusChangedTime(LocalDateTime.now());
            boolean success = taskService.updateById(task);

            // 添加备注和工时
            if (success && comment != null && !comment.isEmpty()) {
                TaskComment taskComment = new TaskComment();
                taskComment.setTaskId(id);
                taskComment.setContent(comment);

                User currentUser = userService.getCurrentUser();
                if (currentUser == null) return Result.error("系统错误，请联系管理员");
                taskComment.setCreatorId(currentUser.getId());
                taskComment.setWorkHours(workHours);
                taskCommentService.addTaskComment(taskComment);
            }

            if (success) {
                String statusText = getTaskStatusText(status);
                taskActivityRecorder.record(id, TaskActivityType.CHANGE_STATUS, statusText);
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

            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return Result.error("系统错误，请联系管理员");
            }
            Long currentUserId = currentUser.getId();

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

                // 设置预计开始时间
                if (subTaskData.get("expectedStartTime") != null) {
                    // 将字符串日期转换为LocalDateTime
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime dateTime = LocalDateTime.parse(subTaskData.get("expectedStartTime").toString(), formatter);
                    subTask.setExpectedStartTime(dateTime);
                }

                // 设置持续时间
                if (subTaskData.get("duration") != null) {
                    subTask.setDuration(Integer.valueOf(subTaskData.get("duration").toString()));
                }

                // 计算期望完成时间
                if (subTask.getExpectedStartTime() != null && subTask.getDuration() != null) {
                    LocalDateTime expectedTime = subTask.getExpectedStartTime().plusDays(subTask.getDuration());
                    subTask.setExpectedTime(expectedTime);
                }

                // 设置前置任务和后置任务
                if (subTaskData.get("predecessorTask") != null) {
                    subTask.setPredecessorTask(subTaskData.get("predecessorTask").toString());
                }
                if (subTaskData.get("postTask") != null) {
                    subTask.setPostTask(subTaskData.get("postTask").toString());
                }

                // 设置内容
                if (subTaskData.get("content") != null) {
                    subTask.setContent(subTaskData.get("content").toString());
                }

                subTask.setCreatorId(currentUserId);

                subTasks.add(subTask);
            }

            boolean success = taskService.splitTask(id, subTasks);

            if (success) {
                // 记录父任务的拆分活动
                taskActivityRecorder.record(id, TaskActivityType.SPLIT_TASK);

                // 为每个子任务记录创建活动
                for (Task subTask : subTasks) {
                    taskActivityRecorder.record(subTask.getId(), TaskActivityType.CREATE_FROM_SPLIT, id);
                }

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
            String expectedStartTime = (String) params.get("expectedStartTime");
            Integer duration = (Integer) params.get("duration");

            if (expectedStartTime != null && expectedStartTime.contains("/")) {
                expectedStartTime = expectedStartTime.replace("/", "-");
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
            LocalDateTime startDateTime = LocalDateTime.parse(expectedStartTime, formatter);

            // 计算期望完成时间
            LocalDateTime endDateTime = startDateTime.plusDays(duration);

            task.setExpectedStartTime(startDateTime);
            task.setExpectedTime(endDateTime);
            task.setDuration(duration);
            task.setUpdateTime(LocalDateTime.now());

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

            if (success) {
                taskActivityRecorder.record(id, TaskActivityType.UPDATE_EXPECTED_TIME, endDateTime.format(formatter));
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
                taskActivityRecorder.record(id, TaskActivityType.ADD_COMMENT);

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
     * 辅助方法：获取任务状态文本
     */
    private String getTaskStatusText(Integer status) {
        return switch (status) {
            case 1 -> "已分派";
            case 2 -> "进行中";
            case 3 -> "已完成";
            default -> "未知";
        };
    }

    /**
     * 辅助方法：获取问题状态文本
     */
    private String getIssueStatusText(Integer status) {
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
                issueMap.put("statusText", getIssueStatusText(issue.getStatus()));
                issueMap.put("expectedTime", issue.getExpectedTime());
                result.add(issueMap);
            }

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取关联问题失败: " + e.getMessage());
        }
    }

    /**
     * 根据任务ID获取任务的父任务
     */
    @GetMapping("/parent_task/{taskId}")
    public Result<Map<String, Object>> getParentTask(@PathVariable Long taskId) {
        try {
            Task task = taskService.getById(taskId);
            if (task == null) {
                return Result.error("任务不存在");
            }

            if (task.getParentTaskId() == null) {
                return Result.success(null);
            }

            Task parentTask = taskService.getById(task.getParentTaskId());
            if (parentTask == null) {
                return Result.success(null);
            }

            // 获取处理人信息
            String handlerName = "";
            if (parentTask.getHandlerId() != null) {
                User handler = userService.getById(parentTask.getHandlerId());
                if (handler != null) {
                    handlerName = handler.getRealName();
                }
            }

            // 获取状态文本
            String statusText = getTaskStatusText(parentTask.getStatus());

            Map<String, Object> result = new HashMap<>();
            result.put("id", parentTask.getId());
            result.put("title", parentTask.getTitle());
            result.put("status", parentTask.getStatus());
            result.put("statusText", statusText);
            result.put("handlerId", parentTask.getHandlerId());
            result.put("handlerName", handlerName);
            result.put("expectedTime", parentTask.getExpectedTime());
            result.put("expectedStartTime", parentTask.getExpectedStartTime());
            result.put("duration", parentTask.getDuration());

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取父任务失败: " + e.getMessage());
        }
    }

    /**
     * 根据父任务ID获取任务的子任务
     */
    @GetMapping("/sub_tasks/{taskId}")
    public Result<List<Map<String, Object>>> getSubTasks(@PathVariable Long taskId) {
        try {
            List<Task> subTasks = taskService.getSubTasks(taskId);
            List<Map<String, Object>> result = new ArrayList<>();

            for (Task subTask : subTasks) {
                // 获取处理人信息
                String handlerName = "";
                if (subTask.getHandlerId() != null) {
                    User handler = userService.getById(subTask.getHandlerId());
                    if (handler != null) {
                        handlerName = handler.getRealName();
                    }
                }

                // 获取状态文本
                String statusText = getTaskStatusText(subTask.getStatus());

                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("id", subTask.getId());
                taskMap.put("title", subTask.getTitle());
                taskMap.put("status", subTask.getStatus());
                taskMap.put("statusText", statusText);
                taskMap.put("handlerId", subTask.getHandlerId());
                taskMap.put("handlerName", handlerName);
                taskMap.put("expectedTime", subTask.getExpectedTime());
                taskMap.put("expectedStartTime", subTask.getExpectedStartTime());
                taskMap.put("duration", subTask.getDuration());

                result.add(taskMap);
            }

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取子任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务的前置任务
     */
    @GetMapping("/pre_tasks/{taskId}")
    public Result<List<Task>> getPreTasks(@PathVariable Long taskId) {
        List<Task> preTasks = taskService.getPreTasks(taskId);
        return Result.success(preTasks);
    }

    /**
     * 获取任务的后置任务
     */
    @GetMapping("/post_tasks/{taskId}")
    public Result<List<Task>> getPostTasks(@PathVariable Long taskId) {
        List<Task> postTasks = taskService.getPostTasks(taskId);
        return Result.success(postTasks);
    }

    /**
     * 获取兄弟任务
     */
    @GetMapping("/brother_tasks/{taskId}")
    public Result<List<Map<String, Object>>> getBrotherTasks(@PathVariable Long taskId) {
        try {
            Task currentTask = taskService.getById(taskId);
            if (currentTask == null) {
                return Result.error("任务不存在");
            }

            // 如果没有父任务，则没有兄弟任务
            if (currentTask.getParentTaskId() == null) {
                return Result.success(new ArrayList<>());
            }

            // 获取所有兄弟任务
            List<Task> brotherTasks = taskService.getBrotherTasks(taskId);
            List<Map<String, Object>> result = new ArrayList<>();

            // 获取前置任务和后置任务
            List<Task> preTasks = taskService.getPreTasks(taskId);
            List<Task> postTasks = taskService.getPostTasks(taskId);

            for (Task brotherTask : brotherTasks) {
                // 获取处理人信息
                String handlerName = "";
                if (brotherTask.getHandlerId() != null) {
                    User handler = userService.getById(brotherTask.getHandlerId());
                    if (handler != null) {
                        handlerName = handler.getRealName();
                    }
                }

                // 获取状态文本
                String statusText = getTaskStatusText(brotherTask.getStatus());

                // 确定任务关系类型
                String relationType = "brother";
                if (preTasks != null && preTasks.stream().anyMatch(t -> t.getId().equals(brotherTask.getId()))) {
                    relationType = "predecessor";
                } else if (postTasks != null && postTasks.stream().anyMatch(t -> t.getId().equals(brotherTask.getId()))) {
                    relationType = "post";
                }

                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("id", brotherTask.getId());
                taskMap.put("title", brotherTask.getTitle());
                taskMap.put("status", brotherTask.getStatus());
                taskMap.put("statusText", statusText);
                taskMap.put("handlerId", brotherTask.getHandlerId());
                taskMap.put("handlerName", handlerName);
                taskMap.put("expectedTime", brotherTask.getExpectedTime());
                taskMap.put("relationType", relationType);

                result.add(taskMap);
            }

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取兄弟任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务进度
     */
    @GetMapping("/activities/{taskId}")
    public Result<List<TaskActivityDTO>> getTaskActivities(@PathVariable Long taskId) {
        try {
            List<TaskActivityDTO> activities = taskActivityService.getActivitiesByTaskId(taskId);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error("获取任务进度失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户的任务列表
     */
    @GetMapping("/current_user")
    public Result<List<TaskDTO>> getCurrentUserTasks() {
        try {
            // 获取当前用户
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }

            // 获取当前用户的所有任务
            List<TaskDTO> tasks = taskService.getCurrentUserTasks(currentUser.getId());
            return Result.success(tasks);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取未完成任务数量
     */
    @GetMapping("/unfinished_count")
    public Result<Integer> getUnfinishedTasksCount() {
        try {
            // 获取当前用户
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }

            // 获取当前用户未完成任务的数量
            int count = taskService.countUnfinishedTasks(currentUser.getId());
            return Result.success(count);
        } catch (Exception e) {
            return Result.error(e.getMessage());
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