package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.Issue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface IssueMapper extends BaseMapper<Issue> {
    @Select("SELECT * FROM issue WHERE task_id = #{taskId}")
    List<Issue> selectByTaskId(Long taskId);
}