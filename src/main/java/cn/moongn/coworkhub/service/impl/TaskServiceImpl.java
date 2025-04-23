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

import java.util.Date;
import java.util.List;
import java.util.Map;
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

        if (task.getExpectedTime() == null) {
            throw new ApiException("期望完成时间不能为空");
        }

        // 设置初始状态为已分派
        task.setStatus(1);

        // 设置创建时间
        task.setCreateTime(new Date());

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
                throw new RuntimeException("父任务不存在");
            }

            for (Task subTask : subTasks) {
                // 继承父任务的部分属性
                subTask.setParentTaskId(parentTaskId);
                subTask.setProjectId(parentTask.getProjectId());
                subTask.setAcceptorId(parentTask.getAcceptorId());
                subTask.setPriority(parentTask.getPriority());
                subTask.setStatus(1); // 设置为"已分派"状态
                subTask.setCreateTime(new Date());
                subTask.setUpdateTime(new Date());

                // 如果子任务没有设置标题，使用父任务的标题
                if (subTask.getTitle() == null || subTask.getTitle().isEmpty()) {
                    subTask.setTitle(parentTask.getTitle() + "-子任务");
                }

                // 如果子任务没有设置内容，使用父任务的内容
                if (subTask.getContent() == null || subTask.getContent().isEmpty()) {
                    subTask.setContent(parentTask.getContent());
                }

                // 保存子任务
                this.save(subTask);
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
                dto.setStatusText("处理中");
                break;
            case 3:
                dto.setStatusText("已完成");
                break;
            case 4:
                dto.setStatusText("测试中");
                break;
            case 5:
                dto.setStatusText("已暂停");
                break;
            case 6:
                dto.setStatusText("已关闭");
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