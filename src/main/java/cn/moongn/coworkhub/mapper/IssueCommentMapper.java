package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.IssueComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface IssueCommentMapper extends BaseMapper<IssueComment> {

    /**
     * 获取问题的所有备注
     */
    @Select("SELECT * FROM issue_comment WHERE issue_id = #{issueId} ORDER BY create_time DESC")
    List<IssueComment> getIssueComments(@Param("issueId") Long issueId);

    /**
     * 分页获取问题备注
     */
    @Select("SELECT * FROM issue_comment WHERE issue_id = #{issueId} ORDER BY create_time DESC")
    Page<IssueComment> pageIssueComments(Page<IssueComment> page, @Param("issueId") Long issueId);
}