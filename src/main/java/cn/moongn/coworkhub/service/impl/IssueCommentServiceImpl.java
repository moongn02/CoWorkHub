package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.mapper.IssueCommentMapper;
import cn.moongn.coworkhub.model.IssueComment;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.service.IssueCommentService;
import cn.moongn.coworkhub.service.UserService;
import cn.moongn.coworkhub.model.dto.IssueCommentDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueCommentServiceImpl extends ServiceImpl<IssueCommentMapper, IssueComment> implements IssueCommentService {

    private final UserService userService;

    @Override
    @Transactional
    public boolean addIssueComment(IssueComment comment) {
        return this.save(comment);
    }

    @Override
    public List<IssueCommentDTO> getIssueComments(Long issueId) {
        List<IssueComment> comments = this.baseMapper.getIssueComments(issueId);
        return comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<IssueCommentDTO> pageIssueComments(Long issueId, int pageNum, int pageSize) {
        Page<IssueComment> page = new Page<>(pageNum, pageSize);
        Page<IssueComment> commentPage = this.baseMapper.pageIssueComments(page, issueId);

        Page<IssueCommentDTO> dtoPage = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        List<IssueCommentDTO> dtoList = commentPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    @Override
    public IssueCommentDTO convertToDTO(IssueComment comment) {
        if (comment == null) {
            return null;
        }

        IssueCommentDTO dto = new IssueCommentDTO();
        BeanUtils.copyProperties(comment, dto);

        // 设置创建人信息
        if (comment.getCreatorId() != null) {
            User creator = userService.getById(comment.getCreatorId());
            if (creator != null) {
                dto.setCreatorName(creator.getRealName());
            }
        }

        return dto;
    }
}