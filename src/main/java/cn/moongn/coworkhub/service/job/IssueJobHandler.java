package cn.moongn.coworkhub.service.job;

import cn.moongn.coworkhub.model.dto.JobConditionDTO;

/**
 * 问题相关作业处理器
 */
public interface IssueJobHandler extends BaseJobHandler {
    boolean execute(JobConditionDTO condition);
    // 继承基础接口的方法即可
}