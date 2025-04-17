package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.TaskActivity;
import cn.moongn.coworkhub.model.dto.TaskActivityDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface TaskActivityService extends IService<TaskActivity> {

    /**
     * 添加活动记录
     * @param activity 活动记录
     * @return 是否成功
     */
    boolean add(TaskActivity activity);

    /**
     * 获取任务的活动记录
     * @param taskId 任务ID
     * @return 活动记录列表
     */
    List<TaskActivityDTO> getActivitiesByTaskId(Long taskId);
}