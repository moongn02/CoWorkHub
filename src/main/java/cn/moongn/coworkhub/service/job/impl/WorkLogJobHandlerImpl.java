package cn.moongn.coworkhub.service.job.impl;

import cn.moongn.coworkhub.common.utils.EmailUtils;
import cn.moongn.coworkhub.mapper.UserMapper;
import cn.moongn.coworkhub.mapper.WorkLogMapper;
import cn.moongn.coworkhub.model.Department;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.JobConditionDTO;
import cn.moongn.coworkhub.service.DepartmentService;
import cn.moongn.coworkhub.service.job.WorkLogJobHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 工作日志相关作业处理器实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkLogJobHandlerImpl implements WorkLogJobHandler {

    private final WorkLogMapper workLogMapper;
    private final UserMapper userMapper;
    private final EmailUtils emailUtils;
    private final DepartmentService departmentService;

    @Override
    public boolean handleDeadlineApproaching(JobConditionDTO condition) {
        log.info("执行工作日志临期提醒作业，条件：{}", condition);
        try {
            // 获取当天日期
            LocalDate today = LocalDate.now();

            // 获取需要提交日志的用户列表（所有状态正常的用户）
            List<User> users = userMapper.selectActiveUsers();
            if (users.isEmpty()) {
                log.info("没有找到需要提交工作日志的用户");
                return true;
            }

            // 查询已提交当日工作日志的用户ID列表
            List<Long> submittedUserIds = workLogMapper.selectUserIdsWithLogOnDate(today);

            // 过滤出需要提醒的用户
            List<User> usersToRemind = new ArrayList<>();
            for (User user : users) {
                if (!submittedUserIds.contains(user.getId())) {
                    usersToRemind.add(user);
                }
            }

            if (usersToRemind.isEmpty()) {
                log.info("所有用户已提交工作日志");
                return true;
            }

            // 发送邮件通知
            for (User user : usersToRemind) {
                sendWorkLogDeadlineNotification(user, today, condition);
            }

            return true;
        } catch (Exception e) {
            log.error("执行工作日志临期提醒作业失败", e);
            throw new RuntimeException("执行工作日志临期提醒作业失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean handleStatusChanged(JobConditionDTO condition) {
        log.info("工作日志不支持状态变更提醒");
        return false;
    }

    @Override
    public boolean handleNewAssignment(JobConditionDTO condition) {
        log.info("工作日志不支持新分配提醒");
        return false;
    }

    @Override
    public boolean handleOverdue(JobConditionDTO condition) {
        log.info("执行工作日志逾期提醒作业，条件：{}", condition);
        try {
            // 获取当前日期
            LocalDate today = LocalDate.now();

            // 获取前一个工作日
            LocalDate previousWorkDay = getPreviousWorkDay(today);

            // 获取需要提交日志的用户列表（所有状态正常的用户）
            List<User> users = userMapper.selectActiveUsers();
            if (users.isEmpty()) {
                log.info("没有找到需要提交工作日志的用户");
                return true;
            }

            // 查询已提交工作日志的用户ID列表
            List<Long> submittedUserIds = workLogMapper.selectUserIdsWithLogOnDate(previousWorkDay);

            // 过滤出未提交日志的用户
            List<User> overdueUsers = new ArrayList<>();
            for (User user : users) {
                if (!submittedUserIds.contains(user.getId())) {
                    overdueUsers.add(user);
                }
            }

            if (overdueUsers.isEmpty()) {
                log.info("所有用户已提交工作日志");
                return true;
            }

            // 发送邮件通知
            for (User user : overdueUsers) {
                sendWorkLogOverdueNotification(user, previousWorkDay, condition);
            }

            return true;
        } catch (Exception e) {
            log.error("执行工作日志逾期提醒作业失败", e);
            throw new RuntimeException("执行工作日志逾期提醒作业失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送工作日志临期通知
     */
    private void sendWorkLogDeadlineNotification(User user, LocalDate deadlineDate, JobConditionDTO condition) {
        if (user.getEmail() == null) {
            log.warn("用户({})没有有效的邮箱地址", user.getId());
            return;
        }

        // 准备收件人列表
        List<String> recipients = new ArrayList<>();
        recipients.add(user.getEmail());

        // 通知管理者
        if (condition.getNotification() != null && Boolean.TRUE.equals(condition.getNotification().getIncludeManagers())
                && user.getDeptId() != null) {
            // 获取部门负责人ID
            Department department = departmentService.getById(user.getDeptId());
            if (department != null && department.getLeaderId() != null) {
                User leader = userMapper.selectById(department.getLeaderId());
                if (leader != null && leader.getEmail() != null) {
                    recipients.add(leader.getEmail());
                }
            }
        }

        // 发送邮件
        String subject = "工作日志提交提醒";
        String content = buildWorkLogDeadlineReminderContent(user, deadlineDate);

        for (String recipient : recipients) {
            boolean success = emailUtils.sendSimpleMail(recipient, subject, content);
            if (success) {
                log.info("已发送工作日志临期提醒邮件给：{}，用户ID：{}", recipient, user.getId());
            } else {
                log.error("发送工作日志临期提醒邮件失败，收件人：{}，用户ID：{}", recipient, user.getId());
            }
        }
    }

    /**
     * 发送工作日志逾期通知
     */
    private void sendWorkLogOverdueNotification(User user, LocalDate dueDatePassed, JobConditionDTO condition) {
        if (user.getEmail() == null) {
            log.warn("用户({})没有有效的邮箱地址", user.getId());
            return;
        }

        // 准备收件人列表
        List<String> recipients = new ArrayList<>();
        recipients.add(user.getEmail());

        // 通知管理者
        if (condition.getNotification() != null && Boolean.TRUE.equals(condition.getNotification().getIncludeManagers())
                && user.getDeptId() != null) {
            // 获取部门负责人ID
            Department department = departmentService.getById(user.getDeptId());
            if (department != null && department.getLeaderId() != null) {
                User leader = userMapper.selectById(department.getLeaderId());
                if (leader != null && leader.getEmail() != null) {
                    recipients.add(leader.getEmail());
                }
            }
        }

        // 发送邮件
        String subject = "工作日志未提交通知";
        String content = buildWorkLogOverdueContent(user, dueDatePassed);

        for (String recipient : recipients) {
            boolean success = emailUtils.sendSimpleMail(recipient, subject, content);
            if (success) {
                log.info("已发送工作日志逾期通知邮件给：{}，用户ID：{}", recipient, user.getId());
            } else {
                log.error("发送工作日志逾期通知邮件失败，收件人：{}，用户ID：{}", recipient, user.getId());
            }
        }
    }

    /**
     * 构建工作日志提醒内容
     */
    private String buildWorkLogDeadlineReminderContent(User user, LocalDate logDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateStr = logDate.format(formatter);

        // 获取用户姓名
        String userName = user.getRealName() != null ? user.getRealName() : user.getUsername();

        // 构建邮件内容
        return "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>" +
                "<h2 style='color: #ff9800;'>今日工作日志提醒</h2>" +
                "<p>下午好~ " + userName + "，</p>" +
                "<p>请记得今天 <b>" + dateStr + "</b> 提交您的工作日志。</p>" +
                "<div style='background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 15px 0;'>" +
                "<p>日志日期：<span style='color: #ff5722;'>" + dateStr + "</span></p>" +
                "<p>请在今天结束前完成工作日志的提交，以便团队能够及时了解您的工作进展。</p>" +
                "</div>" +
                "<p style='margin-top: 20px;'>请登录系统提交工作日志：<a href='http://localhost:5173/work-logs/create' style='color: #1976d2; text-decoration: none;'>点击此处</a></p>" +
                "<p style='color: #888; font-size: 12px; margin-top: 30px; border-top: 1px solid #eee; padding-top: 10px;'>此邮件由系统自动发送，请勿回复。</p>" +
                "</div>";
    }

    /**
     * 构建工作日志逾期通知内容
     */
    private String buildWorkLogOverdueContent(User user, LocalDate dueDatePassed) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dueDateStr = dueDatePassed.format(formatter);

        // 计算逾期天数
        long overdueDays = ChronoUnit.DAYS.between(dueDatePassed, LocalDate.now());

        // 获取用户姓名
        String userName = user.getRealName() != null ? user.getRealName() : user.getUsername();

        // 构建邮件内容
        return "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>" +
                "<h2 style='color: #f44336;'>工作日志未提交通知</h2>" +
                "<p>您好，" + userName + "，</p>" +
                "<p>您 <b>" + dueDateStr + "</b> 的工作日志尚未提交，已逾期 <b>" + overdueDays + " 天</b>。</p>" +
                "<div style='background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 15px 0;'>" +
                "<p>日志应提交日期：<span style='color: #f44336;'>" + dueDateStr + "</span> (已逾期)</p>" +
                "<p>请尽快提交您的工作日志，以便团队能够及时了解您的工作进展。</p>" +
                "</div>" +
                "<p style='margin-top: 20px;'>请登录系统提交工作日志：<a href='http://localhost:5173/work-logs/create' style='color: #1976d2; text-decoration: none;'>点击此处</a></p>" +
                "<p style='color: #888; font-size: 12px; margin-top: 30px; border-top: 1px solid #eee; padding-top: 10px;'>此邮件由系统自动发送，请勿回复。</p>" +
                "</div>";
    }

    /**
     * 获取前一个工作日（简单实现，忽略节假日）
     */
    private LocalDate getPreviousWorkDay(LocalDate date) {
        LocalDate previousDay = date.minusDays(1);
        int dayOfWeek = previousDay.getDayOfWeek().getValue();

        // 如果是周末，则向前再移几天
        if (dayOfWeek == 6) { // 如果是周六，返回周五
            return previousDay.minusDays(1);
        } else if (dayOfWeek == 7) { // 如果是周日，返回周五
            return previousDay.minusDays(2);
        }

        return previousDay;
    }
}