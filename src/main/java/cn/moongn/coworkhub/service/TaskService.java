package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.Task;
import cn.moongn.coworkhub.model.dto.TaskDTO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface TaskService extends IService<Task> {
    /**
     * 创建任务
     * @param task 任务信息
     * @return 是否成功
     */
    boolean createTask(Task task);

    /**
     * 将Task实体转换为DTO
     * @param task 任务实体
     * @return 任务DTO
     */
    TaskDTO convertToDTO(Task task);
}