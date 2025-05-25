package cn.moongn.coworkhub.common.constant.enums;

import lombok.Getter;

@Getter
public enum TaskActivityType {

    CREATE("创建任务"),
    CREATE_FROM_SPLIT("基于父任务拆分创建，任务编号: %s"),
    UPDATE("修改任务"),
    CHANGE_STATUS("变更任务状态为: %s"),
    SYNC_CHANGE_STATUS("同步上级任务变更状态为: %s"),
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