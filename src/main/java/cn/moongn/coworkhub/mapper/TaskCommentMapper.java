package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.TaskComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TaskCommentMapper extends BaseMapper<TaskComment> {

    /**
     * 获取任务的所有备注
     */
    @Select("SELECT * FROM task_comment WHERE task_id = #{taskId} ORDER BY update_time DESC")
    List<TaskComment> getTaskComments(@Param("taskId") Long taskId);

    /**
     * 分页获取任务备注
     */
    @Select("SELECT * FROM task_comment WHERE task_id = #{taskId} ORDER BY create_time DESC")
    Page<TaskComment> pageTaskComments(Page<TaskComment> page, @Param("taskId") Long taskId);
}