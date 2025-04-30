package cn.moongn.coworkhub.service.job.impl;

import cn.moongn.coworkhub.common.utils.EmailUtils;
import cn.moongn.coworkhub.mapper.IssueMapper;
import cn.moongn.coworkhub.mapper.UserMapper;
import cn.moongn.coworkhub.model.Department;
import cn.moongn.coworkhub.model.Issue;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.JobConditionDTO;
import cn.moongn.coworkhub.service.DepartmentService;
import cn.moongn.coworkhub.service.job.IssueJobHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 问题相关作业处理器实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IssueJobHandlerImpl implements IssueJobHandler {

    private final IssueMapper issueMapper;
    private final UserMapper userMapper;
    private final EmailUtils emailUtils;
    private final DepartmentService departmentService;

    @Override
    public boolean execute(JobConditionDTO condition) {
        // 根据触发类型调用对应的处理方法
        String triggerType = condition.getTriggerType();
        if (triggerType == null) {
            log.error("触发类型为空");
            return false;
        }

        switch (triggerType) {
            case "DEADLINE_APPROACHING":
                return handleDeadlineApproaching(condition);
            case "STATUS_CHANGED":
                return handleStatusChanged(condition);
            case "NEW_ASSIGNMENT":
                return handleNewAssignment(condition);
            case "OVERDUE":
                return handleOverdue(condition);
            default:
                log.error("不支持的触发类型: {}", triggerType);
                return false;
        }
    }

    @Override
    public boolean handleDeadlineApproaching(JobConditionDTO condition) {
        log.info("执行问题临期提醒作业，条件：{}", condition);
        try {
            // 获取临期天数设置
            Integer daysBeforeDeadline = condition.getConditions().getDaysBeforeDeadline();
            if (daysBeforeDeadline == null || daysBeforeDeadline <= 0) {
                daysBeforeDeadline = 3; // 默认3天
            }

            // 计算临期日期范围
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime deadlineStart = now.plusDays(daysBeforeDeadline - 1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime deadlineEnd = now.plusDays(daysBeforeDeadline).withHour(23).withMinute(59).withSecond(59);

            // 查询临期问题
            List<Issue> issues = issueMapper.selectDeadlineApproachingIssues(deadlineStart, deadlineEnd);
            if (issues.isEmpty()) {
                log.info("没有找到临期问题");
                return true;
            }

            // 发送邮件通知
            for (Issue issue : issues) {
                sendIssueDeadlineNotification(issue, condition, daysBeforeDeadline);
            }

            return true;
        } catch (Exception e) {
            log.error("执行问题临期提醒作业失败", e);
            throw new RuntimeException("执行问题临期提醒作业失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean handleStatusChanged(JobConditionDTO condition) {
        log.info("执行问题状态变更提醒作业，条件：{}", condition);
        try {
            // 获取状态列表
            List<Integer> statuses = condition.getConditions().getStatuses();
            if (statuses == null || statuses.isEmpty()) {
                log.error("状态列表为空");
                return false;
            }

            // 查询最近状态变更的问题
            LocalDateTime recentTime = LocalDateTime.now().minusMinutes(1); // 最近1分钟内变更的
            List<Issue> issues = issueMapper.selectRecentStatusChangedIssues(statuses, recentTime);
            if (issues.isEmpty()) {
                log.info("没有找到最近状态变更的问题");
                return true;
            }

            // 发送邮件通知
            for (Issue issue : issues) {
                sendIssueStatusChangedNotification(issue, condition);
            }

            return true;
        } catch (Exception e) {
            log.error("执行问题状态变更提醒作业失败", e);
            throw new RuntimeException("执行问题状态变更提醒作业失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean handleNewAssignment(JobConditionDTO condition) {
        log.info("执行问题新分配提醒作业，条件：{}", condition);
        try {
            // 查询最近分配的问题
            LocalDateTime recentTime = LocalDateTime.now().minusMinutes(1); // 最近1分钟内分配的
            List<Issue> issues = issueMapper.selectRecentAssignedIssues(recentTime);
            if (issues.isEmpty()) {
                log.info("没有找到最近分配的问题");
                return true;
            }

            // 发送邮件通知
            for (Issue issue : issues) {
                sendIssueNewAssignmentNotification(issue, condition);
            }

            return true;
        } catch (Exception e) {
            log.error("执行问题新分配提醒作业失败", e);
            throw new RuntimeException("执行问题新分配提醒作业失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean handleOverdue(JobConditionDTO condition) {
        log.info("执行问题逾期提醒作业，条件：{}", condition);
        try {
            // 查询已逾期问题
            LocalDateTime now = LocalDateTime.now();
            List<Issue> issues = issueMapper.selectOverdueIssues(now);
            if (issues.isEmpty()) {
                log.info("没有找到逾期问题");
                return true;
            }

            // 发送邮件通知
            for (Issue issue : issues) {
                sendIssueOverdueNotification(issue, condition);
            }

            return true;
        } catch (Exception e) {
            log.error("执行问题逾期提醒作业失败", e);
            throw new RuntimeException("执行问题逾期提醒作业失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送问题临期通知
     */
    private void sendIssueDeadlineNotification(Issue issue, JobConditionDTO condition, int daysBeforeDeadline) {
        // 准备收件人列表
        List<String> recipients = new ArrayList<>();

        // 问题处理人
        if (issue.getHandlerId() != null) {
            User handler = userMapper.selectById(issue.getHandlerId());
            if (handler != null && handler.getEmail() != null) {
                recipients.add(handler.getEmail());
            }
        }

        // 抄送给创建者
        if (condition.getNotification() != null && Boolean.TRUE.equals(condition.getNotification().getCcToCreator())
                && issue.getCreatorId() != null) {
            User creator = userMapper.selectById(issue.getCreatorId());
            if (creator != null && creator.getEmail() != null) {
                recipients.add(creator.getEmail());
            }
        }

        // 通知管理者
        if (condition.getNotification() != null && Boolean.TRUE.equals(condition.getNotification().getIncludeManagers())
                && issue.getDepartmentId() != null) {
            // 获取部门负责人ID
            Department department = departmentService.getById(issue.getDepartmentId());
            if (department != null && department.getLeaderId() != null) {
                User leader = userMapper.selectById(department.getLeaderId());
                if (leader != null && leader.getEmail() != null) {
                    recipients.add(leader.getEmail());
                }
            }
        }

        // 如果没有收件人则跳过
        if (recipients.isEmpty()) {
            log.warn("问题({})没有有效的收件人", issue.getId());
            return;
        }

        // 发送邮件
        String subject = "问题即将到期提醒";
        String content = buildIssueDeadlineReminderContent(issue, daysBeforeDeadline);

        for (String recipient : recipients) {
            boolean success = emailUtils.sendSimpleMail(recipient, subject, content);
            if (success) {
                log.info("已发送问题临期提醒邮件给：{}，问题ID：{}", recipient, issue.getId());
            } else {
                log.error("发送问题临期提醒邮件失败，收件人：{}，问题ID：{}", recipient, issue.getId());
            }
        }
    }

    /**
     * 发送问题状态变更通知
     */
    private void sendIssueStatusChangedNotification(Issue issue, JobConditionDTO condition) {
        // 准备收件人列表
        List<String> recipients = new ArrayList<>();

        // 问题处理人
        if (issue.getHandlerId() != null) {
            User handler = userMapper.selectById(issue.getHandlerId());
            if (handler != null && handler.getEmail() != null) {
                recipients.add(handler.getEmail());
            }
        }

        // 问题测试人员
        if (issue.getTesterId() != null) {
            User tester = userMapper.selectById(issue.getTesterId());
            if (tester != null && tester.getEmail() != null) {
                recipients.add(tester.getEmail());
            }
        }

        // 抄送给创建者
        if (condition.getNotification() != null && Boolean.TRUE.equals(condition.getNotification().getCcToCreator())
                && issue.getCreatorId() != null) {
            User creator = userMapper.selectById(issue.getCreatorId());
            if (creator != null && creator.getEmail() != null) {
                recipients.add(creator.getEmail());
            }
        }

        // 通知管理者
        if (condition.getNotification() != null && Boolean.TRUE.equals(condition.getNotification().getIncludeManagers())
                && issue.getDepartmentId() != null) {
            // 获取部门负责人ID
            Department department = departmentService.getById(issue.getDepartmentId());
            if (department != null && department.getLeaderId() != null) {
                User leader = userMapper.selectById(department.getLeaderId());
                if (leader != null && leader.getEmail() != null) {
                    recipients.add(leader.getEmail());
                }
            }
        }

        // 如果没有收件人则跳过
        if (recipients.isEmpty()) {
            log.warn("问题({})没有有效的收件人", issue.getId());
            return;
        }

        // 发送邮件
        String subject = "问题状态变更通知";
        String content = buildIssueStatusChangedContent(issue);

        for (String recipient : recipients) {
            boolean success = emailUtils.sendSimpleMail(recipient, subject, content);
            if (success) {
                log.info("已发送问题状态变更通知邮件给：{}，问题ID：{}", recipient, issue.getId());
            } else {
                log.error("发送问题状态变更通知邮件失败，收件人：{}，问题ID：{}", recipient, issue.getId());
            }
        }
    }

    /**
     * 发送问题新分配通知
     */
    private void sendIssueNewAssignmentNotification(Issue issue, JobConditionDTO condition) {
        // 准备收件人列表
        List<String> recipients = new ArrayList<>();

        // 问题处理人（主要接收人）
        if (issue.getHandlerId() != null) {
            User handler = userMapper.selectById(issue.getHandlerId());
            if (handler != null && handler.getEmail() != null) {
                recipients.add(handler.getEmail());
            }
        }

        // 抄送给创建者
        if (condition.getNotification() != null && Boolean.TRUE.equals(condition.getNotification().getCcToCreator())
                && issue.getCreatorId() != null) {
            User creator = userMapper.selectById(issue.getCreatorId());
            if (creator != null && creator.getEmail() != null) {
                recipients.add(creator.getEmail());
            }
        }

        // 通知管理者
        if (condition.getNotification() != null && Boolean.TRUE.equals(condition.getNotification().getIncludeManagers())
                && issue.getDepartmentId() != null) {
            // 获取部门负责人ID
            Department department = departmentService.getById(issue.getDepartmentId());
            if (department != null && department.getLeaderId() != null) {
                User leader = userMapper.selectById(department.getLeaderId());
                if (leader != null && leader.getEmail() != null) {
                    recipients.add(leader.getEmail());
                }
            }
        }

        // 如果没有收件人则跳过
        if (recipients.isEmpty()) {
            log.warn("问题({})没有有效的收件人", issue.getId());
            return;
        }

        // 发送邮件
        String subject = "新问题分配通知";
        String content = buildIssueNewAssignmentContent(issue);

        for (String recipient : recipients) {
            boolean success = emailUtils.sendSimpleMail(recipient, subject, content);
            if (success) {
                log.info("已发送新问题分配通知邮件给：{}，问题ID：{}", recipient, issue.getId());
            } else {
                log.error("发送新问题分配通知邮件失败，收件人：{}，问题ID：{}", recipient, issue.getId());
            }
        }
    }

    /**
     * 发送问题逾期通知
     */
    private void sendIssueOverdueNotification(Issue issue, JobConditionDTO condition) {
        // 准备收件人列表
        List<String> recipients = new ArrayList<>();

        // 问题处理人
        if (issue.getHandlerId() != null) {
            User handler = userMapper.selectById(issue.getHandlerId());
            if (handler != null && handler.getEmail() != null) {
                recipients.add(handler.getEmail());
            }
        }

        // 抄送给创建者
        if (condition.getNotification() != null && Boolean.TRUE.equals(condition.getNotification().getCcToCreator())
                && issue.getCreatorId() != null) {
            User creator = userMapper.selectById(issue.getCreatorId());
            if (creator != null && creator.getEmail() != null) {
                recipients.add(creator.getEmail());
            }
        }

        // 通知管理者
        if (condition.getNotification() != null && Boolean.TRUE.equals(condition.getNotification().getIncludeManagers())
                && issue.getDepartmentId() != null) {
            // 获取部门负责人ID
            Department department = departmentService.getById(issue.getDepartmentId());
            if (department != null && department.getLeaderId() != null) {
                User leader = userMapper.selectById(department.getLeaderId());
                if (leader != null && leader.getEmail() != null) {
                    recipients.add(leader.getEmail());
                }
            }
        }

        // 如果没有收件人则跳过
        if (recipients.isEmpty()) {
            log.warn("问题({})没有有效的收件人", issue.getId());
            return;
        }

        // 发送邮件
        String subject = "问题已逾期通知";
        String content = buildIssueOverdueContent(issue);

        for (String recipient : recipients) {
            boolean success = emailUtils.sendSimpleMail(recipient, subject, content);
            if (success) {
                log.info("已发送问题逾期通知邮件给：{}，问题ID：{}", recipient, issue.getId());
            } else {
                log.error("发送问题逾期通知邮件失败，收件人：{}，问题ID：{}", recipient, issue.getId());
            }
        }
    }

    /**
     * 构建问题临期提醒内容
     */
    private String buildIssueDeadlineReminderContent(Issue issue, int daysBeforeDeadline) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String deadlineStr = issue.getExpectedTime() != null ? issue.getExpectedTime().format(formatter) : "无";

        // 获取问题处理人姓名
        String handlerName = "未指定";
        if (issue.getHandlerId() != null) {
            User handler = userMapper.selectById(issue.getHandlerId());
            if (handler != null) {
                handlerName = handler.getRealName() != null ? handler.getRealName() : handler.getUsername();
            }
        }

        // 获取问题严重程度
        String severityText = getIssueSeverityText(issue.getSeverity());

        // 获取问题状态文本
        String statusText = getIssueStatusText(issue.getStatus());

        // 构建邮件内容
        return "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>" +
                "<h2 style='color: #ff9800;'>问题即将到期提醒</h2>" +
                "<p>您有一个问题将在 <b>" + daysBeforeDeadline + " 天后</b>到期，请及时处理。</p>" +
                "<div style='background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 15px 0;'>" +
                "<p><strong>问题标题：</strong>" + issue.getTitle() + "</p>" +
                "<p><strong>问题状态：</strong>" + statusText + "</p>" +
                "<p><strong>处理人：</strong>" + handlerName + "</p>" +
                "<p><strong>严重程度：</strong>" + severityText + "</p>" +
                "<p><strong>截止日期：</strong><span style='color: #ff5722;'>" + deadlineStr + "</span></p>" +
                "</div>" +
                "<p style='margin-top: 20px;'>请登录系统查看详情并处理：<a href='http://localhost:5173/issues/" + issue.getId() + "' style='color: #1976d2; text-decoration: none;'>点击此处</a></p>" +
                "<p style='color: #888; font-size: 12px; margin-top: 30px; border-top: 1px solid #eee; padding-top: 10px;'>此邮件由系统自动发送，请勿回复。</p>" +
                "</div>";
    }

    /**
     * 构建问题状态变更通知内容
     */
    private String buildIssueStatusChangedContent(Issue issue) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String deadlineStr = issue.getExpectedTime() != null ? issue.getExpectedTime().format(formatter) : "无";

        // 获取问题处理人姓名
        String handlerName = "未指定";
        if (issue.getHandlerId() != null) {
            User handler = userMapper.selectById(issue.getHandlerId());
            if (handler != null) {
                handlerName = handler.getRealName() != null ? handler.getRealName() : handler.getUsername();
            }
        }

        // 获取问题严重程度
        String severityText = getIssueSeverityText(issue.getSeverity());

        // 获取问题状态文本
        String statusText = getIssueStatusText(issue.getStatus());

        // 构建邮件内容
        return "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>" +
                "<h2 style='color: #2196f3;'>问题状态变更通知</h2>" +
                "<p>您关注的问题状态已变更，当前状态为 <b>" + statusText + "</b>。</p>" +
                "<div style='background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 15px 0;'>" +
                "<p><strong>问题标题：</strong>" + issue.getTitle() + "</p>" +
                "<p><strong>问题状态：</strong>" + statusText + "</p>" +
                "<p><strong>处理人：</strong>" + handlerName + "</p>" +
                "<p><strong>严重程度：</strong>" + severityText + "</p>" +
                "<p><strong>截止日期：</strong>" + deadlineStr + "</p>" +
                "</div>" +
                "<p style='margin-top: 20px;'>请登录系统查看详情：<a href='http://localhost:5173/issues/" + issue.getId() + "' style='color: #1976d2; text-decoration: none;'>点击此处</a></p>" +
                "<p style='color: #888; font-size: 12px; margin-top: 30px; border-top: 1px solid #eee; padding-top: 10px;'>此邮件由系统自动发送，请勿回复。</p>" +
                "</div>";
    }

    /**
     * 构建新问题分配通知内容
     */
    private String buildIssueNewAssignmentContent(Issue issue) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String deadlineStr = issue.getExpectedTime() != null ? issue.getExpectedTime().format(formatter) : "无";

        // 获取问题处理人姓名
        String handlerName = "未指定";
        if (issue.getHandlerId() != null) {
            User handler = userMapper.selectById(issue.getHandlerId());
            if (handler != null) {
                handlerName = handler.getRealName() != null ? handler.getRealName() : handler.getUsername();
            }
        }

        // 获取问题创建人姓名
        String creatorName = "未知";
        if (issue.getCreatorId() != null) {
            User creator = userMapper.selectById(issue.getCreatorId());
            if (creator != null) {
                creatorName = creator.getRealName() != null ? creator.getRealName() : creator.getUsername();
            }
        }

        // 获取问题严重程度
        String severityText = getIssueSeverityText(issue.getSeverity());

        // 获取问题状态文本
        String statusText = getIssueStatusText(issue.getStatus());

        // 构建邮件内容
        return "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>" +
                "<h2 style='color: #4caf50;'>新问题分配通知</h2>" +
                "<p>您有一个新问题已分配给您，请及时处理。</p>" +
                "<div style='background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 15px 0;'>" +
                "<p><strong>问题标题：</strong>" + issue.getTitle() + "</p>" +
                "<p><strong>问题状态：</strong>" + statusText + "</p>" +
                "<p><strong>创建人：</strong>" + creatorName + "</p>" +
                "<p><strong>处理人：</strong>" + handlerName + "</p>" +
                "<p><strong>严重程度：</strong>" + severityText + "</p>" +
                "<p><strong>截止日期：</strong>" + deadlineStr + "</p>" +
                "<p><strong>问题描述：</strong></p>" +
                "<div style='background-color: #fff; padding: 10px; border-radius: 3px; border-left: 4px solid #4caf50;'>" +
                issue.getContent() +
                "</div>" +
                "</div>" +
                "<p style='margin-top: 20px;'>请登录系统查看详情并处理：<a href='http://localhost:5173/issues/" + issue.getId() + "' style='color: #1976d2; text-decoration: none;'>点击此处</a></p>" +
                "<p style='color: #888; font-size: 12px; margin-top: 30px; border-top: 1px solid #eee; padding-top: 10px;'>此邮件由系统自动发送，请勿回复。</p>" +
                "</div>";
    }

    /**
     * 构建问题逾期通知内容
     */
    private String buildIssueOverdueContent(Issue issue) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String deadlineStr = issue.getExpectedTime() != null ? issue.getExpectedTime().format(formatter) : "无";

        // 计算逾期天数
        long overdueDays = 0;
        if (issue.getExpectedTime() != null) {
            overdueDays = ChronoUnit.DAYS.between(issue.getExpectedTime().toLocalDate(), LocalDate.now());
        }

        // 获取问题处理人姓名
        String handlerName = "未指定";
        if (issue.getHandlerId() != null) {
            User handler = userMapper.selectById(issue.getHandlerId());
            if (handler != null) {
                handlerName = handler.getRealName() != null ? handler.getRealName() : handler.getUsername();
            }
        }

        // 获取问题严重程度
        String severityText = getIssueSeverityText(issue.getSeverity());

        // 获取问题状态文本
        String statusText = getIssueStatusText(issue.getStatus());

        // 构建邮件内容
        return "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>" +
                "<h2 style='color: #f44336;'>问题已逾期通知</h2>" +
                "<p>您负责的问题已经逾期 <b>" + overdueDays + " 天</b>，请尽快处理。</p>" +
                "<div style='background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 15px 0;'>" +
                "<p><strong>问题标题：</strong>" + issue.getTitle() + "</p>" +
                "<p><strong>问题状态：</strong>" + statusText + "</p>" +
                "<p><strong>处理人：</strong>" + handlerName + "</p>" +
                "<p><strong>严重程度：</strong>" + severityText + "</p>" +
                "<p><strong>截止日期：</strong><span style='color: #f44336;'>" + deadlineStr + "</span> (已逾期)</p>" +
                "</div>" +
                "<p style='margin-top: 20px;'>请登录系统查看详情并尽快处理：<a href='http://localhost:5173/issues/" + issue.getId() + "' style='color: #1976d2; text-decoration: none;'>点击此处</a></p>" +
                "<p style='color: #888; font-size: 12px; margin-top: 30px; border-top: 1px solid #eee; padding-top: 10px;'>此邮件由系统自动发送，请勿回复。</p>" +
                "</div>";
    }

    /**
     * 获取问题状态文本
     */
    private String getIssueStatusText(Integer status) {
        if (status == null) return "未知";

        return switch (status) {
            case 1 -> "已分派";
            case 2 -> "处理中";
            case 3 -> "已解决";
            case 4 -> "已暂停";
            case 5 -> "已关闭";
            default -> "未知状态(" + status + ")";
        };
    }

    /**
     * 获取问题严重程度文本
     */
    private String getIssueSeverityText(Integer severity) {
        if (severity == null) return "未知";

        return switch (severity) {
            case 1 -> "致命";
            case 2 -> "严重";
            case 3 -> "一般";
            case 4 -> "轻微";
            case 5 -> "建议";
            default -> "未知级别(" + severity + ")";
        };
    }
}