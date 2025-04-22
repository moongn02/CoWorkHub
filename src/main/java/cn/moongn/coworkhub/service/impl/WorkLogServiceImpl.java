package cn.moongn.coworkhub.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.moongn.coworkhub.common.exception.ApiException;
import cn.moongn.coworkhub.mapper.WorkLogMapper;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.WorkLog;
import cn.moongn.coworkhub.model.dto.WorkLogDTO;
import cn.moongn.coworkhub.service.UserService;
import cn.moongn.coworkhub.service.WorkLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkLogServiceImpl extends ServiceImpl<WorkLogMapper, WorkLog> implements WorkLogService {

    private final UserService userService;
    private final WorkLogMapper workLogMapper;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public List<WorkLogDTO> getWorkLogsByCurrentUser(LocalDate startDate, LocalDate endDate,
                                                     Integer year, Integer month,
                                                     Integer type) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new ApiException("用户未登录");
        }

        List<WorkLog> logList = workLogMapper.searchWorkLogs(
                currentUser.getId(), startDate, endDate, year, month, type
        );

        return logList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public WorkLogDTO getWorkLogById(Long id) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new ApiException("用户未登录");
        }

        LambdaQueryWrapper<WorkLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WorkLog::getId, id)
                .eq(WorkLog::getUserId, currentUser.getId());

        WorkLog log = this.getOne(queryWrapper);
        if (log == null) {
            throw new ApiException("工作日志不存在");
        }

        return convertToDTO(log);
    }

    @Override
    public boolean createWorkLog(WorkLog workLog) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new ApiException("用户未登录");
        }

        workLog.setUserId(currentUser.getId());

        // 检查是否已存在同一天同一类型的日志
        LambdaQueryWrapper<WorkLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WorkLog::getUserId, currentUser.getId())
                .eq(WorkLog::getLogDate, workLog.getLogDate())
                .eq(WorkLog::getType, workLog.getType());

        WorkLog existingLog = this.getOne(queryWrapper);
        if (existingLog != null) {
            throw new ApiException("当日已存在相同类型的工作日志，请编辑现有日志");
        }

        return this.save(workLog);
    }

    @Override
    public boolean updateWorkLog(WorkLog workLog) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new ApiException("用户未登录");
        }

        // 验证日志是否属于当前用户
        WorkLog existingLog = this.getById(workLog.getId());
        if (existingLog == null || !existingLog.getUserId().equals(currentUser.getId())) {
            throw new ApiException("无权限修改此日志");
        }

        // 保留原始的创建日期
        workLog.setUserId(currentUser.getId());
        workLog.setCreateTime(existingLog.getCreateTime());

        return this.updateById(workLog);
    }

    @Override
    public Map<String, Object> getMonthlyStatistics() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new ApiException("用户未登录");
        }

        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        // 获取当月已提交的日志数量
        int submittedLogs = workLogMapper.getMonthlyLogCount(currentUser.getId(), year, month);

        // 计算当月工作日数量
        int workdays = calculateWorkdaysInCurrentMonth();

        // 计算日志完成百分比
        int percentage = (workdays == 0) ? 0 : (int) Math.min(100, Math.round((double) submittedLogs / workdays * 100));

        Map<String, Object> result = new HashMap<>();
        result.put("submittedLogs", submittedLogs);
        result.put("requiredLogs", workdays);
        result.put("percentage", percentage);

        return result;
    }

    @Override
    public int calculateWorkdaysInCurrentMonth() {
        LocalDate now = LocalDate.now();
        YearMonth yearMonth = YearMonth.of(now.getYear(), now.getMonth());
        int daysInMonth = yearMonth.lengthOfMonth();

        int workdays = 0;
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(now.getYear(), now.getMonth(), day);
            // 如果不是周末，则算作工作日
            // 这里可以扩展，加入节假日的判断
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workdays++;
            }
        }

        return workdays;
    }

    /**
     * 将 WorkLog 转换为 WorkLogDTO
     */
    private WorkLogDTO convertToDTO(WorkLog log) {
        WorkLogDTO dto = BeanUtil.copyProperties(log, WorkLogDTO.class);
        // 设置日志类型文本
        switch (log.getType()) {
            case 1:
                dto.setTypeText("日志");
                break;
            case 2:
                dto.setTypeText("周志");
                break;
            case 3:
                dto.setTypeText("月志");
                break;
            default:
                dto.setTypeText("未知");
        }

        // 格式化日期
        if (log.getLogDate() != null) {
            dto.setLogDateStr(log.getLogDate().format(dateFormatter));
        }

        return dto;
    }
}