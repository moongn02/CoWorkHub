package cn.moongn.coworkhub.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时作业实体类
 */
@Data
@TableName("scheduled_jobs")
public class ScheduledJob {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 定时作业的名称
     */
    private String name;

    /**
     * 定时作业的详细描述
     */
    private String description;

    /**
     * Cron表达式，定义作业的执行时间
     */
    private String cronExpression;

    /**
     * 执行条件，例如任务/问题临期时触发
     */
    private String runCondition;

    /**
     * 作业状态：1-启动，2-暂停
     */
    private Integer status;

    /**
     * 下次执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime nextRunTime;

    /**
     * 记录创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;

    /**
     * 记录最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedTime;
}