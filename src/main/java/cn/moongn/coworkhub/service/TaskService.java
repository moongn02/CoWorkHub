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
     * 获取任务的关联问题
     * @param taskId 任务ID
     * @return 是否成功
     */
    List<Issue> getIssuesByTaskId(Long taskId);

    /**
     * 将Task实体转换为DTO
     * @param task 任务实体
     * @return 任务DTO
     */
    TaskDTO convertToDTO(Task task);

    /**
     * 分页查询任务
     * @param pageNum 当前页
     * @param pageSize 每页大小
     * @param params 查询参数
     * @return 任务分页结果
     */
    Page<TaskDTO> pageTasks(Integer pageNum, Integer pageSize, Map<String, Object> params);
}