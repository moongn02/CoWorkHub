package cn.moongn.coworkhub.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

/**
 * 仪表盘数据访问接口
 */
public interface DashboardMapper {

    /**
     * 获取用户当日任务相关工时
     */
    @Select("SELECT COALESCE(SUM(work_hours), 0) FROM task_comment " +
            "WHERE creator_id = #{userId} AND DATE(create_time) = #{today}")
    double getTodayTaskHours(@Param("userId") Long userId, @Param("today") LocalDate today);

    /**
     * 获取用户当日问题相关工时
     */
    @Select("SELECT COALESCE(SUM(work_hours), 0) FROM issue_comment " +
            "WHERE creator_id = #{userId} AND DATE(create_time) = #{today}")
    double getTodayIssueHours(@Param("userId") Long userId, @Param("today") LocalDate today);

    /**
     * 检查用户是否已填写今日工作日志
     */
    @Select("SELECT COUNT(*) FROM work_log " +
            "WHERE user_id = #{userId} AND log_date = #{today}")
    int checkTodayWorkLog(@Param("userId") Long userId, @Param("today") LocalDate today);
}