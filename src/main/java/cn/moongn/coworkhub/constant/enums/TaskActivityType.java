package cn.moongn.coworkhub.constant.enums;

import lombok.Getter;

@Getter
public enum TaskActivityType {

    CREATE("创建任务"),
    UPDATE("修改任务"),
    CHANGE_STATUS("变更任务状态为: %s"),
    SPLIT_TASK("拆分任务"),
    CREATE_ISSUE("创建关联问题: %s"),
    TRANSFER("将任务转派给: %s"),
    UPDATE_EXPECTED_TIME("修改期望完成时间为: %s"),
    ADD_COMMENT("添加备注");

    private final String template;

    TaskActivityType(String template) {
        this.template = template;
    }

    public String getContent(Object... args) {
        if (args == null || args.length == 0) {
            return this.template;
        }
        return String.format(this.template, args);
    }
}