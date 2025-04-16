package cn.moongn.coworkhub.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IssueActivityDTO {
    private Long id;
    private String content;
    private Long issueId;
    private Long operatorId;
    private String operatorName;  // 操作人姓名
    private LocalDateTime createTime;
}