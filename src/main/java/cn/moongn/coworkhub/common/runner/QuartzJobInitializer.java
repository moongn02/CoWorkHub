package cn.moongn.coworkhub.common.runner;

import cn.moongn.coworkhub.model.ScheduledJob;
import cn.moongn.coworkhub.service.ScheduledJobService;
import cn.moongn.coworkhub.service.job.QuartzJobManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 定时任务初始化器
 * 在系统启动时从数据库加载并初始化所有任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzJobInitializer implements ApplicationRunner {

    private final ScheduledJobService jobService;
    private final QuartzJobManager jobManager;
    private final Scheduler scheduler;

    private static final String JOB_GROUP_NAME = "COWORKHUB_JOB_GROUP";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始初始化定时任务...");

        try {
            // 清理现有任务，确保没有残留任务
            cleanupExistingJobs();

            // 从数据库加载所有任务并初始化
            initializeJobsFromDatabase();

            log.info("定时任务初始化完成");
        } catch (Exception e) {
            log.error("定时任务初始化失败", e);
            throw e;
        }
    }

    /**
     * 清理现有任务，确保没有残留任务
     */
    private void cleanupExistingJobs() throws SchedulerException {
        // 获取当前调度器中所有任务的JobKey
        Set<String> scheduledJobIds = new HashSet<>();
        scheduler.getJobKeys(GroupMatcher.jobGroupEquals(JOB_GROUP_NAME)).forEach(jobKey -> {
            scheduledJobIds.add(jobKey.getName());
        });

        if (!scheduledJobIds.isEmpty()) {
            log.info("发现{}个调度器中的任务，准备清理", scheduledJobIds.size());

            // 删除所有现有任务
            for (String jobId : scheduledJobIds) {
                try {
                    ScheduledJob job = new ScheduledJob();
                    job.setId(Long.valueOf(jobId));
                    jobManager.deleteJob(job);
                    log.info("已清理调度器中的任务: {}", jobId);
                } catch (Exception e) {
                    log.error("清理任务失败, jobId: {}", jobId, e);
                }
            }
        }
    }

    /**
     * 从数据库加载所有任务并初始化
     */
    private void initializeJobsFromDatabase() {
        // 从数据库获取所有任务
        List<ScheduledJob> jobs = jobService.list();

        if (jobs.isEmpty()) {
            log.info("数据库中没有定时任务需要初始化");
            return;
        }

        log.info("从数据库加载到{}个定时任务，开始初始化", jobs.size());

        // 初始化每个任务
        for (ScheduledJob job : jobs) {
            try {
                jobManager.addJob(job);
                log.info("成功初始化任务: {}, ID: {}", job.getName(), job.getId());
            } catch (Exception e) {
                log.error("初始化任务失败, 任务ID: {}, 名称: {}", job.getId(), job.getName(), e);
                // 继续处理其他任务，不中断流程
            }
        }
    }
}