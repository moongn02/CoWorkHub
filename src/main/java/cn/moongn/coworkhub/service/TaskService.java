package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.Issue;
import cn.moongn.coworkhub.model.Task;
import cn.moongn.coworkhub.model.dto.TaskDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface TaskService extends IService<Task> {
    /**
     * 创建任务
     * @param task 任务信息
     * @return 是否成功
     */
    boolean createTask(Task task);

    /**
     * 拆分子任务
     * @param parentTaskId 父任务ID
     * @param subTasks 子任务列表
     * @return 是否成功
     */
    boolean splitTask(Long parentTaskId, List<Task> subTasks);

    /**
     * 获取任务的关联问题
     * @param taskId 任务ID
     * @return 是否成功
     */
    List<Issue> getIssuesByTaskId(Long taskId);

    /**
     * 根据父任务ID获取任务的子任务
     * @param parentTaskId 任务ID
     * @return 子任务列表
     */
    List<Task> getSubTasks(Long parentTaskId);

    /**
     * 根据任务ID获取任务的父任务
     * @param taskId 任务ID
     * @return 父任务列表
     */
    Task getParentTask(Long taskId);

    /**
     * 获取任务的前置任务列表
     * @param taskId 任务ID
     * @return 前置任务列表
     */
    List<Task> getPreTasks(Long taskId);

    /**
     * 获取任务的后置任务列表
     * @param taskId 任务ID
     * @return 后置任务列表
     */
    List<Task> getPostTasks(Long taskId);

    /**
     * 获取兄弟任务列表
     * @param taskId 当前任务ID
     * @return 兄弟任务列表
     */
    List<Task> getBrotherTasks(Long taskId);

    /**
     * 将Task实体转换为DTO
     * @param task 任务实体
     * @return 任务DTO
     */
    TaskDTO convertToDTO(Task task);

    /**
     * 获取当前用户的所有任务
     * @param userId 用户ID
     * @return 任务列表
     */
    List<TaskDTO> getCurrentUserTasks(Long userId);

    /**
     * 统计当前用户未完成任务数量
     * @param userId 用户ID
     * @return 未完成任务数量
     */
    int countUnfinishedTasks(Long userId);

    /**
     * 分页查询任务
     * @param pageNum 当前页
     * @param pageSize 每页大小
     * @param params 查询参数
     * @return 任务分页结果
     */
    Page<TaskDTO> pageTasks(Integer pageNum, Integer pageSize, Map<String, Object> params);
}