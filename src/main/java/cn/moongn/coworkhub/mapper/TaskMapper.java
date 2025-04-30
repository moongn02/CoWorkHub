package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.Task;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    /**
     * 根据父任务ID查询子任务
     */
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

    /**
     * 查询最近状态变更的任务
     */
    @Select("<script>"
            + "SELECT * FROM task "
            + "WHERE status IN "
            + "<foreach collection='statuses' item='status' open='(' separator=',' close=')'>"
            + "#{status}"
            + "</foreach>"
            + " AND last_status_changed_time >= #{recentTime}"
            + "</script>")
    List<Task> selectRecentStatusChangedTasks(
            @Param("statuses") List<Integer> statuses,
            @Param("recentTime") LocalDateTime recentTime);

    /**
     * 查询最近分派的任务
     */
    @Select("SELECT * FROM task WHERE last_assigned_time >= #{recentTime} AND status = 1")
    List<Task> selectRecentAssignedTasks(@Param("recentTime") LocalDateTime recentTime);

    /**
     * 查询临期任务
     * 查找距离截止日期在指定范围内的未完成任务
     */
    @Select("<script>"
            + "SELECT * FROM task "
            + "WHERE status IN (1, 2) " // 状态为已分派或处理中
            + "AND expected_time BETWEEN #{deadlineStart} AND #{deadlineEnd} "
            + "</script>")
    List<Task> selectDeadlineApproachingTasks(
            @Param("deadlineStart") LocalDateTime deadlineStart,
            @Param("deadlineEnd") LocalDateTime deadlineEnd);

    /**
     * 查询已逾期任务
     * 查找截止日期已过但尚未完成的任务
     */
    @Select("SELECT * FROM task WHERE status IN (1, 2) AND expected_time < #{now}")
    List<Task> selectOverdueTasks(@Param("now") LocalDateTime now);
}