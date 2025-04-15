package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.IssueComment;
import cn.moongn.coworkhub.model.dto.IssueCommentDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IssueCommentService extends IService<IssueComment> {

    /**
     * 添加问题备注
     */
    boolean addIssueComment(IssueComment comment);

    /**
     * 获取问题的所有备注
     */
    List<IssueCommentDTO> getIssueComments(Long issueId);

    /**
     * 分页获取问题备注
     */
    Page<IssueCommentDTO> pageIssueComments(Long issueId, int pageNum, int pageSize);

    /**
     * 将 IssueComment 转换为 DTO
     */
    IssueCommentDTO convertToDTO(IssueComment comment);
}