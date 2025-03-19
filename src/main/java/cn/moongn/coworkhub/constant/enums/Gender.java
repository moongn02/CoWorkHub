package cn.moongn.coworkhub.constant.enums;

public enum Gender {
    MALE(0, "男"),
    FEMALE(1, "女");

    private final int code;
    private final String description;

    Gender(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getDescriptionByCode(int code) {
        for (Gender gender : values()) {
            if (gender.code == code) {
                return gender.description;
            }
        }
        return "未知"; // 默认值
    }
}
