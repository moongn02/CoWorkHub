package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.common.exception.ApiException;
import cn.moongn.coworkhub.mapper.*;
import cn.moongn.coworkhub.model.*;
import cn.moongn.coworkhub.model.dto.TaskDTO;
import cn.moongn.coworkhub.service.TaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    private final TaskMapper taskMapper;
    private final IssueMapper issueMapper;
    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final DepartmentMapper departmentMapper;

    /**
     * 创建任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createTask(Task task) {
        // 检查必填字段
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new ApiException("任务标题不能为空");
        }

        if (task.getHandlerId() == null) {
            throw new ApiException("执行人不能为空");
        }

        if (task.getProjectId() == null) {
            throw new ApiException("项目不能为空");
        }

        if (task.getDepartmentId() == null) {
            throw new ApiException("部门不能为空");
        }

        if (task.getExpectedStartTime() == null) {
            throw new ApiException("预计开始时间不能为空");
        }

        if (task.getDuration() == null) {
            throw new ApiException("持续天数不能为空");
        }

        // 设置初始状态为已分派
        task.setStatus(1);

        // 计算期望完成时间
        LocalDateTime expectedTime = task.getExpectedStartTime().plusDays(task.getDuration());
        task.setExpectedTime(expectedTime);

        // 设置创建时间
        task.setCreateTime(LocalDateTime.now());

        // 执行保存
        return this.save(task);
    }

    /**
     * 拆分子任务
     */
    @Override
    @Transactional
    public boolean splitTask(Long parentTaskId, List<Task> subTasks) {
        try {
            // 获取父任务
            Task parentTask = this.getById(parentTaskId);
            if (parentTask == null) {
                throw new ApiException("父任务不存在");
            }

            // 保存子任务并记录ID映射
            Map<Integer, Long> indexToIdMap = new HashMap<>();
            for (int i = 0; i < subTasks.size(); i++) {
                Task subTask = subTasks.get(i);
                // 继承父任务的部分属性
                subTask.setParentTaskId(parentTaskId);
                subTask.setProjectId(parentTask.getProjectId());
                subTask.setAcceptorId(parentTask.getAcceptorId());
                subTask.setPriority(parentTask.getPriority());
                subTask.setStatus(1); // 设置为"已分派"状态
                subTask.setCreateTime(LocalDateTime.now());

                // 如果子任务没有设置标题，使用父任务的标题
                if (subTask.getTitle() == null || subTask.getTitle().isEmpty()) {
                    subTask.setTitle(parentTask.getTitle() + "-子任务");
                }

                // 如果子任务没有设置内容，使用父任务的内容
                if (subTask.getContent() == null || subTask.getContent().isEmpty()) {
                    subTask.setContent(parentTask.getContent());
                }

                // 保存子任务
                boolean saved = this.save(subTask);
                if (!saved) {
                    throw new RuntimeException("保存子任务失败");
                }

                // 获取保存后的子任务ID
                Long subTaskId = subTask.getId();
                if (subTaskId == null) {
                    throw new RuntimeException("获取子任务ID失败");
                }

                // 记录索引到ID的映射
                indexToIdMap.put(i, subTaskId);
            }

            // 更新前置/后置任务关系
            for (Task subTask : subTasks) {
                // 转换前置任务索引为ID
                if (StringUtils.isNotBlank(subTask.getPredecessorTask())) {
                    String[] preIndices = subTask.getPredecessorTask().split(",");
                    String preIds = Arrays.stream(preIndices)
                            .map(index -> indexToIdMap.get(Integer.parseInt(index)).toString())
                            .collect(Collectors.joining(","));
                    subTask.setPredecessorTask(preIds);
                }

                // 转换后置任务索引为ID
                if (StringUtils.isNotBlank(subTask.getPostTask())) {
                    String[] postIndices = subTask.getPostTask().split(",");
                    String postIds = Arrays.stream(postIndices)
                            .map(index -> indexToIdMap.get(Integer.parseInt(index)).toString())
                            .collect(Collectors.joining(","));
                    subTask.setPostTask(postIds);
                }

                // 更新子任务
                boolean updated = this.updateById(subTask);
                if (!updated) {
                    throw new RuntimeException("更新子任务关系失败");
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取关联问题
     */
    @Override
    public List<Issue> getIssuesByTaskId(Long taskId) {
        return issueMapper.selectByTaskId(taskId);
    }

    /**
     * 根据父任务ID获取任务的子任务
     */
    @Override
    public List<Task> getSubTasks(Long parentTaskId) {
        return taskMapper.selectByParentTaskId(parentTaskId);
    }

    /**
     * 根据任务ID获取任务的父任务
     */
    @Override
    public Task getParentTask(Long taskId) {
        Task task = this.getById(taskId);
        if (task != null && task.getParentTaskId() != null) {
            return this.getById(task.getParentTaskId());
        }
        return null;
    }

    /**
     * 获取前置任务
     */
    @Override
    public List<Task> getPreTasks(Long taskId) {
        // 获取当前任务
        Task task = this.getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        // 获取前置任务
        List<Task> preTasks = new ArrayList<>();
        if (StringUtils.isNotBlank(task.getPredecessorTask())) {
            String[] preTaskIds = task.getPredecessorTask().split(",");
            for (String preTaskId : preTaskIds) {
                Task preTask = this.getById(Long.parseLong(preTaskId));
                if (preTask != null) {
                    preTasks.add(preTask);
                }
            }
        }

        return preTasks;
    }

    /**
     * 获取后置任务
     */
    @Override
    public List<Task> getPostTasks(Long taskId) {
        // 获取当前任务
        Task task = this.getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        // 获取后置任务
        List<Task> postTasks = new ArrayList<>();
        if (StringUtils.isNotBlank(task.getPostTask())) {
            String[] postTaskIds = task.getPostTask().split(",");
            for (String postTaskId : postTaskIds) {
                Task postTask = this.getById(Long.parseLong(postTaskId));
                if (postTask != null) {
                    postTasks.add(postTask);
                }
            }
        }

        return postTasks;
    }

    @Override
    public List<TaskDTO> getCurrentUserTasks(Long userId) {
        List<Task> tasks = taskMapper.selectCurrentUserTasks(userId);

        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public int countUnfinishedTasks(Long userId) {
        return taskMapper.countUnfinishedTasks(userId);
    }

    @Override
    public List<Task> getBrotherTasks(Long taskId) {
        // 获取当前任务
        Task currentTask = this.getById(taskId);
        if (currentTask == null) {
            throw new RuntimeException("任务不存在");
        }

        // 如果没有父任务，则没有兄弟任务
        if (currentTask.getParentTaskId() == null) {
            return new ArrayList<>();
        }

        // 构建查询条件
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Task::getParentTaskId, currentTask.getParentTaskId())
                .ne(Task::getId, taskId)
                .orderByAsc(Task::getCreateTime);

        // 查询兄弟任务
        return this.list(queryWrapper);
    }

    /**
     * 分页查询任务
     */
    @Override
    public Page<TaskDTO> pageTasks(Integer pageNum, Integer pageSize, Map<String, Object> params) {
        // 创建分页对象
        Page<Task> page = new Page<>(pageNum, pageSize);

        // 构建条件
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();

        // 添加条件
        String id = (String) params.get("id");
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.eq(Task::getId, id);
        }

        String title = (String) params.get("title");
        if (StringUtils.isNotBlank(title)) {
            queryWrapper.like(Task::getTitle, title);
        }

        Long creatorId = (Long) params.get("creatorId");
        if (creatorId != null) {
            queryWrapper.eq(Task::getCreatorId, creatorId);
        }

        Long handlerId = (Long) params.get("handlerId");
        if (handlerId != null) {
            queryWrapper.eq(Task::getHandlerId, handlerId);
        }

        Long acceptorId = (Long) params.get("acceptorId");
        if (acceptorId != null) {
            queryWrapper.eq(Task::getAcceptorId, acceptorId);
        }

        Long projectId = (Long) params.get("projectId");
        if (projectId != null) {
            queryWrapper.eq(Task::getProjectId, projectId);
        }

        Long departmentId = (Long) params.get("departmentId");
        if (departmentId != null) {
            queryWrapper.eq(Task::getDepartmentId, departmentId);
        }

        Long parentTaskId = (Long) params.get("parentTaskId");
        if (parentTaskId != null) {
            queryWrapper.eq(Task::getParentTaskId, parentTaskId);
        }

        Integer status = (Integer) params.get("status");
        if (status != null) {
            queryWrapper.eq(Task::getStatus, status);
        }

        Integer priority = (Integer) params.get("priority");
        if (priority != null) {
            queryWrapper.eq(Task::getPriority, priority);
        }

        // 添加期望完成时间范围搜索
        String startDate = (String) params.get("startDate");
        String endDate = (String) params.get("endDate");
        if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            queryWrapper.between(Task::getExpectedTime, startDate + "00:00:00", endDate + " 23:59:59");
        }

        // 添加排序
        queryWrapper.orderByAsc(Task::getCreateTime);

        // 执行查询
        Page<Task> resultPage = this.page(page, queryWrapper);

        // 转换为DTO
        Page<TaskDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<TaskDTO> dtoList = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    @Override
    public TaskDTO convertToDTO(Task task) {
        if (task == null) {
            return null;
        }

        TaskDTO dto = new TaskDTO();
        BeanUtils.copyProperties(task, dto);

        // 设置状态文本
        switch (task.getStatus()) {
            case 1:
                dto.setStatusText("已分派");
                break;
            case 2:
                dto.setStatusText("进行中");
                break;
            case 3:
                dto.setStatusText("已完成");
                break;
            default:
                dto.setStatusText("未知状态");
        }

        // 设置优先级文本
        switch (task.getPriority()) {
            case 1:
                dto.setPriorityText("高优先级");
                break;
            case 2:
                dto.setPriorityText("中优先级");
                break;
            case 3:
                dto.setPriorityText("低优先级");
                break;
            default:
                dto.setPriorityText("未知优先级");
        }

        // 获取相关名称
        if (task.getCreatorId() != null) {
            User creator = userMapper.selectById(task.getCreatorId());
            if (creator != null) {
                dto.setCreatorName(creator.getRealName());
            }
        }

        if (task.getHandlerId() != null) {
            User handler = userMapper.selectById(task.getHandlerId());
            if (handler != null) {
                dto.setHandlerName(handler.getRealName());
            }
        }

        if (task.getAcceptorId() != null) {
            User acceptor = userMapper.selectById(task.getAcceptorId());
            if (acceptor != null) {
                dto.setAcceptorName(acceptor.getRealName());
            }
        }

        if (task.getProjectId() != null) {
            Project project = projectMapper.selectById(task.getProjectId());
            if (project != null) {
                dto.setProjectName(project.getName());
            }
        }

        if (task.getDepartmentId() != null) {
            Department department = departmentMapper.selectById(task.getDepartmentId());
            if (department != null) {
                dto.setDepartmentName(department.getName());
            }
        }

        return dto;
    }
}