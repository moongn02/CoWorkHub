package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.Task;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    // 根据父任务ID查询子任务
    @Select("SELECT * FROM task WHERE parent_task_id = #{parentTaskId}")
    List<Task> selectByParentTaskId(Long parentTaskId);

    /**
     * 获取当前用户的所有任务
     */
    @Select("<script>"
            + "SELECT t.* FROM task t "
            + "WHERE t.handler_id = #{userId} "
            + "AND t.status IN (1, 2) "
            + "ORDER BY t.create_time DESC"
            + "</script>")
    List<Task> selectCurrentUserTasks(@Param("userId") Long userId);

    /**
     * 统计当前用户未完成任务数量
     */
    @Select("<script>"
            + "SELECT COUNT(*) FROM task t "
            + "WHERE t.handler_id = #{userId} "
            + "AND t.status IN (1, 2) "
            + "</script>")
    int countUnfinishedTasks(@Param("userId") Long userId);
}