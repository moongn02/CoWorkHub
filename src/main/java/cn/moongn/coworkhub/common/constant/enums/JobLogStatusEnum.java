package cn.moongn.coworkhub.common.constant.enums;

import lombok.Getter;

/**
 * 定时任务执行日志状态枚举
 */
@Getter
public enum JobLogStatusEnum {
    SUCCESS(1, "成功"),
    FAILED(2, "失败");

    private final Integer code;
    private final String desc;

    JobLogStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static JobLogStatusEnum getByCode(Integer code) {
        for (JobLogStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    public static String getDescByCode(Integer code) {
        JobLogStatusEnum status = getByCode(code);
        return status != null ? status.getDesc() : "";
    }
}
