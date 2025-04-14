package cn.moongn.coworkhub.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IssueDTO {
    private Long id;
    private String title;
    private String content;
    private Long creatorId;
    private String creatorName;
    private Long testerId;
    private String testerName;
    private Long handlerId;
    private String handlerName;
    private Long projectId;
    private String projectName;
    private Long departmentId;
    private String departmentName;
    private Long testRound;
    private Long taskId;
    private String taskTitle;
    private Integer type;
    private String typeText;
    private Integer bugCause;
    private String bugCauseText;
    private String otherBugCause;
    private Integer status;
    private String statusText;
    private Integer severity;
    private String severityText;
    private Integer urgency;
    private String urgencyText;
    private Integer browser;
    private String browserText;
    private String otherBrowser;
    private Integer platform;
    private String platformText;
    private Integer sys;
    private String sysText;
    private String otherSys;
    private LocalDateTime expectedTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}