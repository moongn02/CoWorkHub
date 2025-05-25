package cn.moongn.coworkhub.common.constant.enums;

import lombok.Getter;

/**
 * 定时任务状态枚举
 */
@Getter
public enum JobStatusEnum {
    NORMAL(1, "启动"),
    PAUSED(2, "暂停");

    private final Integer code;
    private final String desc;

    JobStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static JobStatusEnum getByCode(Integer code) {
        for (JobStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    public static String getDescByCode(Integer code) {
        JobStatusEnum status = getByCode(code);
        return status != null ? status.getDesc() : "";
    }
}
