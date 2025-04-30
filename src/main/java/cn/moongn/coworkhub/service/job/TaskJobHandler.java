package cn.moongn.coworkhub.service.job;

import cn.moongn.coworkhub.model.dto.JobConditionDTO;

/**
 * 任务相关作业处理器
 */
public interface TaskJobHandler extends BaseJobHandler {
    boolean execute(JobConditionDTO condition);

}