package cn.moongn.coworkhub.model.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WorkLogDTO {
    private Long id;
    private Long userId;
    private String content;
    private Integer type; // 日志类型：1-日志 2-周志 3-月志
    private String typeText; // 日志类型文本表示
    private LocalDate logDate;
    private String logDateStr; // 格式化的日期字符串
    private String username; // 用户名
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
