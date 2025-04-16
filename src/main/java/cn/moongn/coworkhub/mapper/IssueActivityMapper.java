package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.IssueActivity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IssueActivityMapper extends BaseMapper<IssueActivity> {

    @Select("SELECT * FROM issue_activity WHERE issue_id = #{issueId} ORDER BY create_time DESC")
    List<IssueActivity> selectByIssueId(Long issueId);
}