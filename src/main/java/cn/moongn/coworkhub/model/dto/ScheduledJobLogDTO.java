package cn.moongn.coworkhub.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时作业日志DTO
 */
@Data
public class ScheduledJobLogDTO {
    private Long id;

    private Long jobId;

    private String jobName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime executionTime;

    private Integer status;

    private String statusText;

    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;
}