package cn.moongn.coworkhub.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("task_comment")
public class TaskComment {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String content;

    private Long creatorId;

    private Long taskId;

    private BigDecimal workHours;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}