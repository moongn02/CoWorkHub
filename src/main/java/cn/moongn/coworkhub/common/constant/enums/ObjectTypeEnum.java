package cn.moongn.coworkhub.common.constant.enums;

import lombok.Getter;

/**
 * 定时任务对象类型枚举
 */
@Getter
public enum ObjectTypeEnum {
    TASK(1, "任务"),
    ISSUE(2, "问题"),
    WORK_LOG(3, "工作日志");

    private final Integer code;
    private final String desc;

    ObjectTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ObjectTypeEnum getByCode(Integer code) {
        for (ObjectTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static String getDescByCode(Integer code) {
        ObjectTypeEnum type = getByCode(code);
        return type != null ? type.getDesc() : "";
    }
}