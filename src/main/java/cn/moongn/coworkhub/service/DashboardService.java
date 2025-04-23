package cn.moongn.coworkhub.service;

import java.util.Map;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {

    /**
     * 获取当前用户的仪表盘统计数据
     * @param userId 用户ID
     * @return 包含统计数据的Map
     */
    Map<String, Object> getUserDashboardStats(Long userId);

    /**
     * 获取当前用户未完成任务数量
     * @param userId 用户ID
     * @return 未完成任务数量
     */
    int getUnfinishedTasksCount(Long userId);

    /**
     * 获取当前用户未解决问题数量
     * @param userId 用户ID
     * @return 未解决问题数量
     */
    int getUnresolvedIssuesCount(Long userId);

    /**
     * 获取当前用户今日工时
     * @param userId 用户ID
     * @return 今日工时
     */
    double getTodayWorkHours(Long userId);

    /**
     * 检查用户今日是否已填写工作日志
     * @param userId 用户ID
     * @return 是否已填写
     */
    boolean hasTodayWorkLog(Long userId);
}