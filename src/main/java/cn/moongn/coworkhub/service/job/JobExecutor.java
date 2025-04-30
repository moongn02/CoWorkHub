package cn.moongn.coworkhub.service.job;

import cn.moongn.coworkhub.constant.enums.JobLogStatusEnum;
import cn.moongn.coworkhub.constant.enums.ObjectTypeEnum;
import cn.moongn.coworkhub.constant.enums.TriggerTypeEnum;
import cn.moongn.coworkhub.model.dto.JobConditionDTO;
import cn.moongn.coworkhub.model.ScheduledJob;
import cn.moongn.coworkhub.service.ScheduledJobLogService;
import cn.moongn.coworkhub.service.ScheduledJobService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.LocalDateTime;

/**
 * Quartz任务执行器
 */
@Slf4j
@DisallowConcurrentExecution  // 禁止并发执行同一个任务
public class JobExecutor extends QuartzJobBean {

    @Autowired
    private ScheduledJobService jobService;

    @Autowired
    private ScheduledJobLogService jobLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskJobHandler taskJobHandler;

    @Autowired
    private IssueJobHandler issueJobHandler;

    @Autowired
    private WorkLogJobHandler workLogJobHandler;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // 获取任务ID
        Long jobId = context.getMergedJobDataMap().getLong("jobId");
        log.info("开始执行定时任务，任务ID: {}", jobId);

        try {
            // 获取任务信息
            ScheduledJob job = jobService.getById(jobId);
            if (job == null) {
                log.error("任务不存在，任务ID: {}", jobId);
                return;
            }

            // 解析任务条件
            JobConditionDTO condition = objectMapper.readValue(job.getRunCondition(), JobConditionDTO.class);

            // 根据对象类型和触发类型执行不同的处理逻辑
            boolean success = false;
            String message = "";

            try {
                success = executeJobByType(condition);
                message = success ? "执行成功" : "执行失败";
            } catch (Exception e) {
                // 捕获详细的异常信息，不需要截断异常信息
                message = "执行异常: " + e.getMessage();
                // 添加堆栈跟踪信息
                StackTraceElement[] stackTrace = e.getStackTrace();
                if (stackTrace.length > 0) {
                    StringBuilder stackTraceStr = new StringBuilder(message);
                    stackTraceStr.append("\n堆栈跟踪:");

                    // 添加完整的堆栈信息，便于调试
                    for (int i = 0; i < Math.min(10, stackTrace.length); i++) {
                        stackTraceStr.append("\n  at ").append(stackTrace[i]);
                    }

                    if (stackTrace.length > 10) {
                        stackTraceStr.append("\n  ... ").append(stackTrace.length - 10).append(" more");
                    }

                    message = stackTraceStr.toString();
                }

                log.error("任务执行异常，任务ID: {}", jobId, e);
            }

            // 记录执行日志
            jobLogService.addJobLog(
                    jobId,
                    success ? JobLogStatusEnum.SUCCESS.getCode() : JobLogStatusEnum.FAILED.getCode(),
                    message
            );

            // 更新任务下次执行时间
            job.setNextRunTime(LocalDateTime.now().plusSeconds(context.getNextFireTime().getTime() / 1000));
            job.setUpdatedTime(LocalDateTime.now());
            jobService.updateById(job);

            log.info("定时任务执行完成，任务ID: {}, 结果: {}", jobId, success ? "成功" : "失败");
        } catch (Exception e) {
            log.error("执行定时任务失败", e);
            throw new JobExecutionException(e);
        }
    }

    /**
     * 根据对象类型和触发类型执行不同的处理逻辑
     */
    private boolean executeJobByType(JobConditionDTO condition) {
        String objectType = condition.getObjectType();
        String triggerType = condition.getTriggerType();

        // 任务处理
        if (ObjectTypeEnum.TASK.name().equals(objectType)) {
            return executeTaskJob(triggerType, condition);
        }
        // 问题处理
        else if (ObjectTypeEnum.ISSUE.name().equals(objectType)) {
            return executeIssueJob(triggerType, condition);
        }
        // 工作日志处理
        else if (ObjectTypeEnum.WORK_LOG.name().equals(objectType)) {
            return executeWorkLogJob(triggerType, condition);
        }
        else {
            log.error("不支持的对象类型: {}", objectType);
            return false;
        }
    }

    /**
     * 执行任务相关的定时处理
     */
    private boolean executeTaskJob(String triggerType, JobConditionDTO condition) {
        if (TriggerTypeEnum.DEADLINE_APPROACHING.name().equals(triggerType)) {
            return taskJobHandler.handleDeadlineApproaching(condition);
        } else if (TriggerTypeEnum.STATUS_CHANGED.name().equals(triggerType)) {
            return taskJobHandler.handleStatusChanged(condition);
        } else if (TriggerTypeEnum.NEW_ASSIGNMENT.name().equals(triggerType)) {
            return taskJobHandler.handleNewAssignment(condition);
        } else if (TriggerTypeEnum.OVERDUE.name().equals(triggerType)) {
            return taskJobHandler.handleOverdue(condition);
        } else {
            log.error("不支持的触发类型: {}", triggerType);
            return false;
        }
    }

    /**
     * 执行问题相关的定时处理
     */
    private boolean executeIssueJob(String triggerType, JobConditionDTO condition) {
        if (TriggerTypeEnum.DEADLINE_APPROACHING.name().equals(triggerType)) {
            return issueJobHandler.handleDeadlineApproaching(condition);
        } else if (TriggerTypeEnum.STATUS_CHANGED.name().equals(triggerType)) {
            return issueJobHandler.handleStatusChanged(condition);
        } else if (TriggerTypeEnum.NEW_ASSIGNMENT.name().equals(triggerType)) {
            return issueJobHandler.handleNewAssignment(condition);
        } else if (TriggerTypeEnum.OVERDUE.name().equals(triggerType)) {
            return issueJobHandler.handleOverdue(condition);
        } else {
            log.error("不支持的触发类型: {}", triggerType);
            return false;
        }
    }

    /**
     * 执行工作日志相关的定时处理
     */
    private boolean executeWorkLogJob(String triggerType, JobConditionDTO condition) {
        if(TriggerTypeEnum.DEADLINE_APPROACHING.name().equals(triggerType)) {
            return workLogJobHandler.handleDeadlineApproaching(condition);
        }else if (TriggerTypeEnum.OVERDUE.name().equals(triggerType)) {
            return workLogJobHandler.handleOverdue(condition);
        }else {
            log.error("工作日志不支持的触发类型: {}", triggerType);
            return false;
        }
    }
}