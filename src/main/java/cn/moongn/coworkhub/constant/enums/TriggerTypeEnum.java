package cn.moongn.coworkhub.constant.enums;

import lombok.Getter;

/**
 * 定时任务触发类型枚举
 */
@Getter
public enum TriggerTypeEnum {
    DEADLINE_APPROACHING(1, "临期提醒"),
    STATUS_CHANGED(2, "状态变更"),
    NEW_ASSIGNMENT(3, "新分配提醒"),
    OVERDUE(4, "已逾期提醒");

    private final Integer code;
    private final String desc;

    TriggerTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TriggerTypeEnum getByCode(Integer code) {
        for (TriggerTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static String getDescByCode(Integer code) {
        TriggerTypeEnum type = getByCode(code);
        return type != null ? type.getDesc() : "";
    }
}
