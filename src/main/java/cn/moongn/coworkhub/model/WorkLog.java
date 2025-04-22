package cn.moongn.coworkhub.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("work_log")
public class WorkLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String content;
    private Integer type; // 日志类型：1-日志 2-周志 3-月志
    private LocalDate logDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}