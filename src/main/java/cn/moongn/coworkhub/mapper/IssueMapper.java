package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.Issue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

public interface IssueMapper extends BaseMapper<Issue> {
    /**
     * 根据任务ID查询关联问题
     */
    @Select("SELECT * FROM issue WHERE task_id = #{taskId}")
    List<Issue> selectByTaskId(Long taskId);

    /**
     * 获取当前用户的所有问题
     */
    @Select("<script>"
            + "SELECT i.* FROM issue i "
            + "WHERE i.handler_id = #{userId} "
            + "AND i.status IN (1, 2) "
            + "ORDER BY i.create_time DESC"
            + "</script>")
    List<Issue> selectCurrentUserIssues(@Param("userId") Long userId);

    /**
     * 统计当前用户未解决问题数量
     */
    @Select("<script>"
            + "SELECT COUNT(*) FROM issue i "
            + "WHERE i.handler_id = #{userId} "
            + "AND i.status IN (1, 2) "
            + "</script>")
    int countUnresolvedIssues(@Param("userId") Long userId);

    /**
     * 查询最近状态变更的问题
     */
    @Select("<script>"
            + "SELECT * FROM issue "
            + "WHERE status IN "
            + "<foreach collection='statuses' item='status' open='(' separator=',' close=')'>"
            + "#{status}"
            + "</foreach>"
            + " AND last_status_changed_time >= #{recentTime}"
            + "</script>")
    List<Issue> selectRecentStatusChangedIssues(
            @Param("statuses") List<Integer> statuses,
            @Param("recentTime") LocalDateTime recentTime);

    /**
     * 查询最近分配的问题
     */
    @Select("SELECT * FROM issue WHERE last_assigned_time >= #{recentTime} AND status = 1")
    List<Issue> selectRecentAssignedIssues(@Param("recentTime") LocalDateTime recentTime);

    /**
     * 查询临期问题
     * 查找距离截止日期在指定范围内的未解决问题
     */
    @Select("<script>"
            + "SELECT * FROM issue "
            + "WHERE status IN (1, 2) " // 状态为已分派或处理中
            + "AND expected_time BETWEEN #{deadlineStart} AND #{deadlineEnd} "
            + "</script>")
    List<Issue> selectDeadlineApproachingIssues(
            @Param("deadlineStart") LocalDateTime deadlineStart,
            @Param("deadlineEnd") LocalDateTime deadlineEnd);

    /**
     * 查询已逾期问题
     * 查找截止日期已过但尚未解决的问题
     */
    @Select("SELECT * FROM issue WHERE status IN (1, 2) AND expected_time < #{now}")
    List<Issue> selectOverdueIssues(@Param("now") LocalDateTime now);
}