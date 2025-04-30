package cn.moongn.coworkhub.service.job;

import cn.moongn.coworkhub.model.dto.JobConditionDTO;

/**
 * 工作日志相关作业处理器
 */
public interface WorkLogJobHandler {
    boolean handleDeadlineApproaching(JobConditionDTO condition);

    boolean handleStatusChanged(JobConditionDTO condition);

    boolean handleNewAssignment(JobConditionDTO condition);

    boolean handleOverdue(JobConditionDTO condition);
}