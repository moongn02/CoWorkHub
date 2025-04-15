package cn.moongn.coworkhub.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class IssueCommentDTO {

    private Long id;

    private String content;

    private Long creatorId;

    private String creatorName;

    private Long issueId;

    private BigDecimal workHours;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}