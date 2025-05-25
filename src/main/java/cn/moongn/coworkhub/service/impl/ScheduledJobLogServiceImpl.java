package cn.moongn.coworkhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.moongn.coworkhub.common.constant.enums.JobLogStatusEnum;
import cn.moongn.coworkhub.model.dto.ScheduledJobLogDTO;
import cn.moongn.coworkhub.mapper.ScheduledJobLogMapper;
import cn.moongn.coworkhub.mapper.ScheduledJobMapper;
import cn.moongn.coworkhub.model.ScheduledJob;
import cn.moongn.coworkhub.model.ScheduledJobLog;
import cn.moongn.coworkhub.service.ScheduledJobLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 定时任务日志服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledJobLogServiceImpl extends ServiceImpl<ScheduledJobLogMapper, ScheduledJobLog> implements ScheduledJobLogService {

    private final ScheduledJobMapper jobMapper;

    @Override
    public IPage<ScheduledJobLogDTO> getLogsByPage(long current, long size, Long jobId, Integer status) {
        Page<ScheduledJobLog> page = new Page<>(current, size);

        // 查询日志记录
        IPage<ScheduledJobLog> logPage = baseMapper.selectLogPage(page, jobId, status);

        // 获取所有相关任务的ID和名称映射
        Map<Long, String> jobNameMap = new HashMap<>();
        if (!logPage.getRecords().isEmpty()) {
            logPage.getRecords().forEach(log -> {
                if (!jobNameMap.containsKey(log.getJobId())) {
                    ScheduledJob job = jobMapper.selectById(log.getJobId());
                    if (job != null) {
                        jobNameMap.put(job.getId(), job.getName());
                    }
                }
            });
        }

        // 转换为DTO
        IPage<ScheduledJobLogDTO> dtoPage = logPage.convert(log -> {
            ScheduledJobLogDTO dto = new ScheduledJobLogDTO();
            BeanUtils.copyProperties(log, dto);
            dto.setJobName(jobNameMap.getOrDefault(log.getJobId(), "未知任务"));
            dto.setStatusText(JobLogStatusEnum.getDescByCode(log.getStatus()));
            return dto;
        });

        return dtoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addJobLog(Long jobId, Integer status, String message) {
        ScheduledJobLog log = new ScheduledJobLog();
        log.setJobId(jobId);
        log.setStatus(status);
        log.setMessage(message);
        log.setExecutionTime(LocalDateTime.now());
        log.setCreatedTime(LocalDateTime.now());

        baseMapper.insert(log);
    }

    @Override
    public ScheduledJobLogDTO getLogDetail(Long id) {
        ScheduledJobLog log = getById(id);
        if (log == null) {
            return null;
        }

        ScheduledJobLogDTO dto = new ScheduledJobLogDTO();
        BeanUtils.copyProperties(log, dto);

        // 获取任务名称
        ScheduledJob job = jobMapper.selectById(log.getJobId());
        if (job != null) {
            dto.setJobName(job.getName());
        } else {
            dto.setJobName("未知任务");
        }

        dto.setStatusText(JobLogStatusEnum.getDescByCode(log.getStatus()));
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanExpiredLogs(int days) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<ScheduledJobLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(ScheduledJobLog::getCreatedTime, expireTime);

        int count = (int) count(wrapper);
        if (count > 0) {
            remove(wrapper);
            log.info("成功清理{}条过期日志", count);
        }

        return count;
    }
}