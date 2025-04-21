package cn.moongn.coworkhub.model.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WorkMemoDTO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private LocalDate memoDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}