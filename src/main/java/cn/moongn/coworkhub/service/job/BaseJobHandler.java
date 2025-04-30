package cn.moongn.coworkhub.service.job;

import cn.moongn.coworkhub.model.dto.JobConditionDTO;

/**
 * 作业处理器基础接口
 */
public interface BaseJobHandler {

    /**
     * 处理临期提醒
     */
    boolean handleDeadlineApproaching(JobConditionDTO condition);

    /**
     * 处理状态变更
     */
    boolean handleStatusChanged(JobConditionDTO condition);

    /**
     * 处理新分配提醒
     */
    boolean handleNewAssignment(JobConditionDTO condition);

    /**
     * 处理逾期提醒
     */
    boolean handleOverdue(JobConditionDTO condition);
}