package cn.moongn.coworkhub.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_activity")
public class TaskActivity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String content;

    private Long taskId;

    private Long operatorId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
