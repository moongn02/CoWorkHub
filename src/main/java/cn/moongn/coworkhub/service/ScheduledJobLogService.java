package cn.moongn.coworkhub.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.moongn.coworkhub.model.ScheduledJobLog;
import cn.moongn.coworkhub.model.dto.ScheduledJobLogDTO;

/**
 * 定时任务日志服务接口
 */
public interface ScheduledJobLogService extends IService<ScheduledJobLog> {

    /**
     * 分页查询任务执行日志
     * @param current 当前页
     * @param size 每页条数
     * @param jobId 任务ID
     * @param status 执行状态
     * @return 分页结果
     */
    IPage<ScheduledJobLogDTO> getLogsByPage(long current, long size, Long jobId, Integer status);

    /**
     * 添加任务执行日志
     *
     * @param jobId   任务ID
     * @param status  执行状态
     * @param message 执行消息
     */
    void addJobLog(Long jobId, Integer status, String message);

    /**
     * 获取任务执行日志详情
     * @param id 日志ID
     * @return 日志详情
     */
    ScheduledJobLogDTO getLogDetail(Long id);

    /**
     * 清理过期日志
     * @param days 保留天数
     * @return 清理数量
     */
    int cleanExpiredLogs(int days);
}