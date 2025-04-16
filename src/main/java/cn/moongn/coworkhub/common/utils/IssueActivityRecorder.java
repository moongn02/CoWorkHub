package cn.moongn.coworkhub.common.utils;

import cn.moongn.coworkhub.constant.enums.IssueActivityType;
import cn.moongn.coworkhub.model.IssueActivity;
import cn.moongn.coworkhub.service.IssueActivityService;
import cn.moongn.coworkhub.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;


@Component
public class IssueActivityRecorder {

    @Resource
    private IssueActivityService issueActivityService;

    @Resource
    private UserService userService;

    /**
     * 记录问题活动
     * @param issueId 问题ID
     * @param type 活动类型
     * @param args 格式化参数
     */
    public void record(Long issueId, IssueActivityType type, Object... args) {
        if (issueId == null) {
            return;
        }

        // 获取当前用户
        Long currentUserId = userService.getCurrentUser().getId();

        IssueActivity activity = new IssueActivity();
        activity.setIssueId(issueId);
        activity.setOperatorId(currentUserId);
        activity.setContent(type.getContent(args));

        issueActivityService.add(activity);
    }
}