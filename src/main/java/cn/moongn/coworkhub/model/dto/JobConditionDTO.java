package cn.moongn.coworkhub.model.dto;

import lombok.Data;
import java.util.List;

/**
 * 任务条件DTO，用于封装JSON条件
 */
@Data
public class JobConditionDTO {
    private String objectType;
    private String triggerType;
    private ConditionParamsDTO conditions;
    private NotificationParamsDTO notification;
    private List<Long> taskIds;
    private List<Long> issueIds;

    @Data
    public static class ConditionParamsDTO {
        private List<Integer> statuses;
        private Integer daysBeforeDeadline;
        private List<Integer> priorities;
        private List<Integer> severities;
        private Integer unchangedDays;
        // 其他可能的条件参数
    }

    @Data
    public static class NotificationParamsDTO {
        private String template;
        private Boolean ccToCreator;
        private Boolean includeManagers;
        // 其他通知相关参数
    }
}