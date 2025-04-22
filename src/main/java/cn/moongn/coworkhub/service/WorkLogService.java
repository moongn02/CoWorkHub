package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.WorkLog;
import cn.moongn.coworkhub.model.dto.WorkLogDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface WorkLogService extends IService<WorkLog> {
    /**
     * 获取当前用户的工作日志列表
     */
    List<WorkLogDTO> getWorkLogsByCurrentUser(LocalDate startDate, LocalDate endDate,
                                              Integer year, Integer month,
                                              Integer type);

    /**
     * 获取工作日志详情
     */
    WorkLogDTO getWorkLogById(Long id);

    /**
     * 创建工作日志
     */
    boolean createWorkLog(WorkLog workLog);

    /**
     * 更新工作日志
     */
    boolean updateWorkLog(WorkLog workLog);

    /**
     * 获取本月工作日志统计信息
     */
    Map<String, Object> getMonthlyStatistics();

    /**
     * 计算当月工作日数量（不包括周末和节假日）
     */
    int calculateWorkdaysInCurrentMonth();
}