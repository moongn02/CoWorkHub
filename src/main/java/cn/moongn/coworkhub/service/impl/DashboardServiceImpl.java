package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.mapper.DashboardMapper;
import cn.moongn.coworkhub.service.DashboardService;
import cn.moongn.coworkhub.service.IssueService;
import cn.moongn.coworkhub.service.TaskService;
import cn.moongn.coworkhub.service.WorkLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMapper dashboardMapper;
    private final TaskService taskService;
    private final IssueService issueService;
    private final WorkLogService workLogService;

    @Override
    public Map<String, Object> getUserDashboardStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        // 获取未完成任务数量
        int unfinishedTasks = getUnfinishedTasksCount(userId);
        stats.put("unfinishedTasks", unfinishedTasks);

        // 获取未解决问题数量
        int unresolvedIssues = getUnresolvedIssuesCount(userId);
        stats.put("unresolvedIssues", unresolvedIssues);

        // 获取今日工时
        double todayHours = getTodayWorkHours(userId);
        stats.put("todayHours", todayHours);

        // 检查今日是否已填写工作日志
        boolean hasTodayLog = hasTodayWorkLog(userId);
        stats.put("hasTodayLog", hasTodayLog);

        return stats;
    }

    @Override
    public int getUnfinishedTasksCount(Long userId) {
        return taskService.countUnfinishedTasks(userId);
    }

    @Override
    public int getUnresolvedIssuesCount(Long userId) {
        return issueService.countUnresolvedIssues(userId);
    }

    @Override
    public double getTodayWorkHours(Long userId) {
        LocalDate today = LocalDate.now();

        // 获取任务相关工时
        double taskHours = dashboardMapper.getTodayTaskHours(userId, today);

        // 获取问题相关工时
        double issueHours = dashboardMapper.getTodayIssueHours(userId, today);

        // 返回总工时
        return taskHours + issueHours;
    }

    @Override
    public boolean hasTodayWorkLog(Long userId) {
        return dashboardMapper.checkTodayWorkLog(userId, LocalDate.now()) > 0;
    }
}