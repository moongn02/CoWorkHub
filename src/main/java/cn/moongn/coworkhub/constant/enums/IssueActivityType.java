package cn.moongn.coworkhub.constant.enums;

import lombok.Getter;

@Getter
public enum IssueActivityType {

    CREATE("创建问题"),
    UPDATE("修改问题"),
    CHANGE_STATUS("变更问题状态为: %s"),
    TRANSFER("将问题转派给: %s"),
    UPDATE_EXPECTED_TIME("修改期望完成时间为: %s"),
    ADD_COMMENT("添加备注"),
    RELATED_TASK("修改关联任务为: %s");

    private final String template;

    IssueActivityType(String template) {
        this.template = template;
    }

    public String getContent(Object... args) {
        if (args == null || args.length == 0) {
            return this.template;
        }
        return String.format(this.template, args);
    }
}