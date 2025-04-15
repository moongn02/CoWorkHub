package cn.moongn.coworkhub.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TaskCommentDTO {

    private Long id;

    private String content;

    private Long creatorId;

    private String creatorName;

    private Long taskId;

    private BigDecimal workHours;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}