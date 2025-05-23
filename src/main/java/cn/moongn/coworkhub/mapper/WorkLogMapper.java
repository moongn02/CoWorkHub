package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.WorkLog;
import cn.moongn.coworkhub.model.dto.WorkLogDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface WorkLogMapper extends BaseMapper<WorkLog> {
    /**
     * 获取用户在指定月份的工作日志数量
     */
    @Select("SELECT COUNT(*) FROM work_log WHERE user_id = #{userId} AND YEAR(log_date) = #{year} AND MONTH(log_date) = #{month}")
    int getMonthlyLogCount(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    /**
     * 获取今日工作日志
     */
    @Select("SELECT * FROM work_log WHERE user_id = #{userId} AND log_date = #{today} AND type = 1 LIMIT 1")
    WorkLogDTO selectTodayLog(@Param("userId") Long userId, @Param("today") LocalDate today);

    /**
     * 统计本周工作日志数量
     */
    @Select("SELECT COUNT(*) FROM work_log WHERE user_id = #{userId} AND log_date BETWEEN #{weekStart} AND #{weekEnd}")
    int countWeekWorkLogs(@Param("userId") Long userId, @Param("weekStart") LocalDate weekStart, @Param("weekEnd") LocalDate weekEnd);

    /**
     * 按条件查询工作日志列表
     */
    @Select("<script>" +
            "SELECT * FROM work_log WHERE user_id = #{userId} " +
            "<if test='startDate != null'> AND log_date &gt;= #{startDate} </if>" +
            "<if test='endDate != null'> AND log_date &lt;= #{endDate} </if>" +
            "<if test='year != null'> AND YEAR(log_date) = #{year} </if>" +
            "<if test='month != null'> AND MONTH(log_date) = #{month} </if>" +
            "<if test='type != null'> AND type = #{type} </if>" +
            "ORDER BY log_date DESC" +
            "</script>")
    List<WorkLog> searchWorkLogs(@Param("userId") Long userId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate,
                                 @Param("year") Integer year,
                                 @Param("month") Integer month,
                                 @Param("type") Integer type);

    /**
     * 查询指定日期已提交工作日志的用户ID列表
     */
    @Select("SELECT DISTINCT user_id FROM work_log WHERE log_date = #{logDate}")
    List<Long> selectUserIdsWithLogOnDate(@Param("logDate") LocalDate logDate);

    /**
     * 查询指定日期范围内已提交工作日志的用户ID列表
     */
    @Select("SELECT DISTINCT user_id FROM work_log WHERE log_date BETWEEN #{startDate} AND #{endDate}")
    List<Long> selectUserIdsWithLogBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 查询指定用户在指定日期是否已提交工作日志
     */
    @Select("SELECT COUNT(*) FROM work_log WHERE user_id = #{userId} AND log_date = #{logDate}")
    int countUserLogOnDate(@Param("userId") Long userId, @Param("logDate") LocalDate logDate);

    /**
     * 查询指定日期范围内每个用户提交的工作日志数量
     */
    @Select("SELECT user_id, COUNT(*) as log_count FROM work_log " +
            "WHERE log_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY user_id")
    List<Object[]> countUserLogsBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}