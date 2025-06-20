package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.common.exception.ApiException;
import cn.moongn.coworkhub.service.job.QuartzJobManager;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.moongn.coworkhub.common.constant.enums.JobStatusEnum;
import cn.moongn.coworkhub.model.dto.JobConditionDTO;
import cn.moongn.coworkhub.model.dto.ScheduledJobDTO;
import cn.moongn.coworkhub.mapper.ScheduledJobMapper;
import cn.moongn.coworkhub.model.ScheduledJob;
import cn.moongn.coworkhub.service.ScheduledJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.quartz.SchedulerException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 定时作业服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledJobServiceImpl extends ServiceImpl<ScheduledJobMapper, ScheduledJob> implements ScheduledJobService {

    private final QuartzJobManager quartzJobManager;
    private final ObjectMapper objectMapper;
    @Autowired
    private ScheduledJobMapper scheduledJobMapper;

    @Override
    public IPage<ScheduledJobDTO> getJobsByPage(long current, long size, String name, Integer status, String objectType, String triggerType) {
        Page<ScheduledJob> page = new Page<>(current, size);
        Page<ScheduledJob> jobPage = baseMapper.selectJobPage(page, name, status, objectType, triggerType);

        // 转换为DTO
        return jobPage.convert(this::convertToDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addJob(ScheduledJobDTO jobDTO) {
        try {
            // 验证cron表达式
            try {
                CronExpression.validateExpression(jobDTO.getCronExpression());
            } catch (Exception e) {
                throw new ApiException("Cron表达式无效: " + e.getMessage());
            }

            // 验证条件JSON格式
            validateRunCondition(jobDTO.getRunCondition());

            // 转换为实体并保存
            ScheduledJob job = new ScheduledJob();
            BeanUtils.copyProperties(jobDTO, job);
            job.setStatus(JobStatusEnum.PAUSED.getCode());
            job.setCreatedTime(LocalDateTime.now());
            job.setUpdatedTime(LocalDateTime.now());
            baseMapper.insert(job);

            // 创建Quartz作业
            try {
                quartzJobManager.addJob(job);

                pauseJob(job.getId());
            } catch (SchedulerException e) {
                log.error("添加定时作业失败", e);
                throw new ApiException("添加定时作业失败: " + e.getMessage());
            }

            return job.getId();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("添加定时作业失败", e);
            throw new ApiException("添加定时作业失败: " + e.getMessage());
        }
    }

    // 验证条件JSON格式
    private void validateRunCondition(String runCondition) {
        if (StringUtils.isBlank(runCondition)) {
            return;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(runCondition);
        } catch (Exception e) {
            throw new ApiException("运行条件格式不正确: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateJob(ScheduledJobDTO jobDTO) {
        try {
            // 获取原作业信息
            ScheduledJob oldJob = getById(jobDTO.getId());
            if (oldJob == null) {
                throw new ApiException("作业不存在");
            }

            // 验证条件JSON格式
            validateCondition(jobDTO.getRunCondition());

            // 验证cron表达式
            quartzJobManager.validateCronExpression(jobDTO.getCronExpression());

            // 更新作业
            ScheduledJob job = new ScheduledJob();
            BeanUtils.copyProperties(jobDTO, job);
            job.setUpdatedTime(LocalDateTime.now());

            // 如果修改了cron表达式，需要重新计算下次执行时间
            if (!oldJob.getCronExpression().equals(job.getCronExpression())) {
                job.setNextRunTime(quartzJobManager.getNextRunTime(job.getCronExpression()));
            }

            boolean result = updateById(job);

            // 更新Quartz作业
            quartzJobManager.updateJob(job);

            // 清理可能存在的孤立作业
            List<Long> validJobIds = this.list().stream()
                    .map(ScheduledJob::getId)
                    .collect(Collectors.toList());
            quartzJobManager.cleanupOrphanedJobs(validJobIds);

            return result;
        } catch (SchedulerException e) {
            log.error("修改定时作业失败", e);
            throw new ApiException("修改定时作业失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteJob(Long id) {
        try {
            // 获取作业信息
            ScheduledJob job = getById(id);
            if (job == null) {
                throw new ApiException("作业不存在");
            }

            // 删除Quartz作业
            quartzJobManager.deleteJob(job);

            // 删除数据库记录
            boolean result = removeById(id);

            // 清理可能存在的孤立作业
            List<Long> validJobIds = this.list().stream()
                    .map(ScheduledJob::getId)
                    .collect(Collectors.toList());
            quartzJobManager.cleanupOrphanedJobs(validJobIds);

            return result;
        } catch (SchedulerException e) {
            log.error("删除定时作业失败", e);
            throw new ApiException("删除定时作业失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteJobs(List<Long> ids) {
        try {
            for (Long id : ids) {
                ScheduledJob job = getById(id);
                if (job != null) {
                    // 删除Quartz作业
                    quartzJobManager.deleteJob(job);
                }
            }

            // 批量删除数据库记录
            boolean result = removeByIds(ids);

            // 清理可能存在的孤立作业
            List<Long> validJobIds = this.list().stream()
                    .map(ScheduledJob::getId)
                    .collect(Collectors.toList());
            quartzJobManager.cleanupOrphanedJobs(validJobIds);

            return result;
        } catch (SchedulerException e) {
            log.error("批量删除定时作业失败", e);
            throw new ApiException("批量删除定时作业失败: " + e.getMessage());
        }
    }

    @Override
    public ScheduledJobDTO getJobDetail(Long id) {
        ScheduledJob job = getById(id);
        if (job == null) {
            return null;
        }
        return convertToDTO(job);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean pauseJob(Long id) {
        try {
            // 获取作业信息
            ScheduledJob job = getById(id);
            if (job == null) {
                throw new ApiException("作业不存在");
            }

            // 暂停Quartz作业
            quartzJobManager.pauseJob(job);

            boolean result = update(
                    new LambdaUpdateWrapper<ScheduledJob>()
                            .eq(ScheduledJob::getId, id)
                            .set(ScheduledJob::getStatus, JobStatusEnum.PAUSED.getCode())
                            .set(ScheduledJob::getNextRunTime, null)
                            .set(ScheduledJob::getUpdatedTime, LocalDateTime.now())
            );

            return result;
        } catch (SchedulerException e) {
            log.error("暂停定时作业失败", e);
            throw new ApiException("暂停定时作业失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resumeJob(Long id) {
        try {
            // 获取作业信息
            ScheduledJob job = getById(id);
            if (job == null) {
                throw new ApiException("作业不存在");
            }

            // 恢复作业
            job.setStatus(JobStatusEnum.NORMAL.getCode());
            job.setUpdatedTime(LocalDateTime.now());
            job.setNextRunTime(quartzJobManager.getNextRunTime(job.getCronExpression()));
            boolean result = updateById(job);

            // 恢复Quartz作业
            quartzJobManager.resumeJob(job);

            return result;
        } catch (SchedulerException e) {
            log.error("恢复定时作业失败", e);
            throw new ApiException("恢复定时作业失败: " + e.getMessage());
        }
    }

    @Override
    public boolean triggerJob(Long id) {
        try {
            // 获取作业信息
            ScheduledJob job = getById(id);
            if (job == null) {
                throw new ApiException("作业不存在");
            }

            // 立即执行Quartz作业
            quartzJobManager.triggerJob(job);

            // 仅当任务为启用状态时更新下次执行时间
            if (job.getStatus() == JobStatusEnum.NORMAL.getCode()) {
                job.setNextRunTime(quartzJobManager.getNextRunTime(job.getCronExpression()));
                job.setUpdatedTime(LocalDateTime.now());
                updateById(job);
            }

            return true;
        } catch (SchedulerException e) {
            log.error("立即执行定时作业失败", e);
            throw new ApiException("立即执行定时作业失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateAllNextRunTime() {
        List<ScheduledJob> jobs = scheduledJobMapper.selectList(null);
        for (ScheduledJob job : jobs) {
            if (Objects.equals(job.getStatus(), JobStatusEnum.PAUSED.getCode())) {
                continue;
            }
            LocalDateTime nextTime = quartzJobManager.getNextRunTime(job.getCronExpression());
            job.setNextRunTime(nextTime);
            scheduledJobMapper.updateById(job);
        }
    }

    /**
     * 将实体转换为DTO
     */
    private ScheduledJobDTO convertToDTO(ScheduledJob job) {
        if (job == null) {
            return null;
        }

        ScheduledJobDTO dto = new ScheduledJobDTO();
        BeanUtils.copyProperties(job, dto);

        // 设置状态文本
        if (job.getStatus() != null) {
            dto.setStatusText(job.getStatus() == 1 ? "启动" : "暂停");
        }

        return dto;
    }

    /**
     * 验证条件JSON是否有效
     */
    private void validateCondition(String conditionJson) {
        try {
            JobConditionDTO condition = objectMapper.readValue(conditionJson, JobConditionDTO.class);
            // 可以添加更多的验证逻辑
        } catch (JsonProcessingException e) {
            throw new ApiException("条件JSON格式无效: " + e.getMessage());
        }
    }
}