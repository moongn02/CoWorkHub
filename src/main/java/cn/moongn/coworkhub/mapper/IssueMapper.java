package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.Issue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface IssueMapper extends BaseMapper<Issue> {
    // 根据任务ID查询关联问题
    @Select("SELECT * FROM issue WHERE task_id = #{taskId}")
    List<Issue> selectByTaskId(Long taskId);

    /**
     * 获取当前用户的所有问题
     */
    @Select("<script>"
            + "SELECT i.* FROM issue i "
            + "WHERE i.handler_id = #{userId} "
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
}