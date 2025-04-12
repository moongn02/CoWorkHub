package cn.moongn.coworkhub.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String content;
    private Long creatorId;
    private String creatorName;
    private Long handlerId;
    private String handlerName;
    private Long acceptorId;
    private String acceptorName;
    private Long projectId;
    private String projectName;
    private Long departmentId;
    private String departmentName;
    private Long parentTaskId;
    private Integer status;
    private String statusText;
    private Integer priority;
    private String priorityText;
    private Date actualStartTime;
    private Date expectedTime;
}