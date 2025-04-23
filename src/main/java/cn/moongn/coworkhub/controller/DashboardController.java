package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.service.DashboardService;
import cn.moongn.coworkhub.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 仪表盘控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    /**
     * 获取当前用户的仪表盘统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getDashboardStats() {
        try {
            // 获取当前用户
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }

            // 获取仪表盘统计数据
            Map<String, Object> stats = dashboardService.getUserDashboardStats(currentUser.getId());
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取仪表盘统计数据失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取未完成任务数量
     */
    @GetMapping("/unfinished_tasks")
    public Result<Integer> getUnfinishedTasksCount() {
        try {
            // 获取当前用户
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }

            // 获取未完成任务数量
            int count = dashboardService.getUnfinishedTasksCount(currentUser.getId());
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取未完成任务数量失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取未解决问题数量
     */
    @GetMapping("/unresolved_issues")
    public Result<Integer> getUnresolvedIssuesCount() {
        try {
            // 获取当前用户
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }

            // 获取未解决问题数量
            int count = dashboardService.getUnresolvedIssuesCount(currentUser.getId());
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取未解决问题数量失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取今日工时
     */
    @GetMapping("/today_hours")
    public Result<Double> getTodayWorkHours() {
        try {
            // 获取当前用户
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }

            // 获取今日工时
            double hours = dashboardService.getTodayWorkHours(currentUser.getId());
            return Result.success(hours);
        } catch (Exception e) {
            log.error("获取今日工时失败", e);
            return Result.error(e.getMessage());
        }
    }
}