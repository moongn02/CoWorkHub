package cn.moongn.coworkhub.service.job;

import cn.moongn.coworkhub.model.ScheduledJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Quartz任务管理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzJobManager {

    private final Scheduler scheduler;

    private static final String JOB_GROUP_NAME = "COWORKHUB_JOB_GROUP";
    private static final String TRIGGER_GROUP_NAME = "COWORKHUB_TRIGGER_GROUP";

    /**
     * 创建定时任务
     */
    public void addJob(ScheduledJob job) throws SchedulerException {
        // 创建JobDetail
        JobDetail jobDetail = JobBuilder.newJob(JobExecutor.class)
                .withIdentity(getJobKey(job))
                .withDescription(job.getDescription())
                .usingJobData("jobId", job.getId())
                .build();

        // 创建Trigger
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(getTriggerKey(job))
                .withDescription(job.getName())
                .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()))
                .build();

        // 计算下次执行时间并更新
        Date nextFireTime = trigger.getNextFireTime();

        // 添加这个检查
        if (nextFireTime != null) {
            LocalDateTime nextRunTime = LocalDateTime.ofInstant(
                    nextFireTime.toInstant(), ZoneId.systemDefault());
            job.setNextRunTime(nextRunTime);
        } else {
            // 处理无法获取下次执行时间的情况
            log.warn("Cannot calculate next fire time for job: {}, cron expression: {}",
                    job.getName(), job.getCronExpression());
            // 设置一个默认的执行时间，或者抛出一个更具体的异常
            // 例如，设置为当前时间加一小时
            job.setNextRunTime(LocalDateTime.now().plusHours(1));
        }

        // 注册任务
        scheduler.scheduleJob(jobDetail, trigger);

        // 如果任务已暂停，则暂停调度
        if (job.getStatus() == 2) {
            scheduler.pauseJob(getJobKey(job));
        }
    }

    /**
     * 更新定时任务
     */
    public void updateJob(ScheduledJob job) throws SchedulerException {
        // 获取触发器
        TriggerKey triggerKey = getTriggerKey(job);
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

        if (trigger == null) {
            // 如果触发器不存在，则创建新的任务
            addJob(job);
            return;
        }

        // 更新cron表达式
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
        trigger = trigger.getTriggerBuilder()
                .withIdentity(triggerKey)
                .withDescription(job.getName())
                .withSchedule(scheduleBuilder)
                .build();

        // 重新调度任务
        scheduler.rescheduleJob(triggerKey, trigger);

        // 更新任务状态
        Integer status = job.getStatus();
        if (status != null) {
            if (status == 1) {
                resumeJob(job);
            } else if (status == 2) {
                pauseJob(job);
            }
        }

        // 计算并更新下次执行时间
        updateNextRunTime(job);
    }

    /**
     * 更新任务的下次执行时间
     */
    private void updateNextRunTime(ScheduledJob job) throws SchedulerException {
        Trigger trigger = scheduler.getTrigger(getTriggerKey(job));
        if (trigger != null && trigger.getNextFireTime() != null) {
            LocalDateTime nextRunTime = LocalDateTime.ofInstant(
                    trigger.getNextFireTime().toInstant(), ZoneId.systemDefault());
            job.setNextRunTime(nextRunTime);
        } else {
            log.warn("无法获取任务下次执行时间, 任务ID: {}", job.getId());
        }
    }

    /**
     * 删除定时任务
     */
    public void deleteJob(ScheduledJob job) throws SchedulerException {
        JobKey jobKey = getJobKey(job);
        // 确保任务存在再删除
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
            log.info("已删除定时任务, 任务ID: {}", job.getId());
        } else {
            log.warn("要删除的定时任务不存在, 任务ID: {}", job.getId());
        }
    }

    /**
     * 清理过期任务
     * 删除调度器中存在但数据库中已不存在的任务
     */
    public void cleanupOrphanedJobs(List<Long> validJobIds) throws SchedulerException {
        // 获取当前调度器中所有任务的JobKey
        Set<String> scheduledJobIds = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(JOB_GROUP_NAME))
                .stream()
                .map(JobKey::getName)
                .collect(Collectors.toSet());

        // 转换有效任务ID为字符串集合
        Set<String> validJobIdsStr = validJobIds.stream()
                .map(String::valueOf)
                .collect(Collectors.toSet());

        // 找出孤立的任务ID
        scheduledJobIds.removeAll(validJobIdsStr);

        // 删除孤立任务
        for (String jobId : scheduledJobIds) {
            try {
                JobKey jobKey = JobKey.jobKey(jobId, JOB_GROUP_NAME);
                scheduler.deleteJob(jobKey);
                log.info("已清理孤立任务, 任务ID: {}", jobId);
            } catch (Exception e) {
                log.error("清理孤立任务失败, 任务ID: {}", jobId, e);
            }
        }
    }

    /**
     * 暂停定时任务
     */
    public void pauseJob(ScheduledJob job) throws SchedulerException {
        scheduler.pauseJob(getJobKey(job));
    }

    /**
     * 恢复定时任务
     */
    public void resumeJob(ScheduledJob job) throws SchedulerException {
        scheduler.resumeJob(getJobKey(job));
    }

    /**
     * 立即执行一次任务
     */
    public void triggerJob(ScheduledJob job) throws SchedulerException {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("jobId", job.getId());
        scheduler.triggerJob(getJobKey(job), dataMap);
    }

    /**
     * 验证cron表达式是否有效
     */
    public boolean validateCronExpression(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

    /**
     * 获取下次执行时间
     */
    public LocalDateTime getNextFireTime(String cronExpression) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            Date nextFireTime = cron.getNextValidTimeAfter(new Date());
            if (nextFireTime != null) {
                return LocalDateTime.ofInstant(
                        nextFireTime.toInstant(), ZoneId.systemDefault());
            }
        } catch (Exception e) {
            log.error("计算下次执行时间失败", e);
        }
        return null;
    }

    /**
     * 获取JobKey
     */
    private JobKey getJobKey(ScheduledJob job) {
        return JobKey.jobKey(job.getId().toString(), JOB_GROUP_NAME);
    }

    /**
     * 获取TriggerKey
     */
    private TriggerKey getTriggerKey(ScheduledJob job) {
        return TriggerKey.triggerKey(job.getId().toString(), TRIGGER_GROUP_NAME);
    }
}