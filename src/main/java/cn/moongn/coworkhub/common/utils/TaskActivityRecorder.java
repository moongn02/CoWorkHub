package cn.moongn.coworkhub.common.utils;

import cn.moongn.coworkhub.constant.enums.TaskActivityType;
import cn.moongn.coworkhub.model.TaskActivity;
import cn.moongn.coworkhub.service.TaskActivityService;
import cn.moongn.coworkhub.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class TaskActivityRecorder {

    @Resource
    private TaskActivityService taskActivityService;

    @Resource
    private UserService userService;

    /**
     * 记录任务活动
     * @param taskId 任务ID
     * @param type 活动类型
     * @param args 格式化参数
     */
    public void record(Long taskId, TaskActivityType type, Object... args) {
        if (taskId == null) {
            return;
        }

        // 获取当前用户
        Long currentUserId = userService.getCurrentUser().getId();

        TaskActivity activity = new TaskActivity();
        activity.setTaskId(taskId);
        activity.setOperatorId(currentUserId);
        activity.setContent(type.getContent(args));

        taskActivityService.add(activity);
    }
}