package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.mapper.TaskCommentMapper;
import cn.moongn.coworkhub.model.TaskComment;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.service.TaskCommentService;
import cn.moongn.coworkhub.service.UserService;
import cn.moongn.coworkhub.model.dto.TaskCommentDTO;
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
public class TaskCommentServiceImpl extends ServiceImpl<TaskCommentMapper, TaskComment> implements TaskCommentService {

    private final UserService userService;

    @Override
    @Transactional
    public boolean addTaskComment(TaskComment comment) {
        return this.save(comment);
    }

    @Override
    public List<TaskCommentDTO> getTaskComments(Long taskId) {
        List<TaskComment> comments = this.baseMapper.getTaskComments(taskId);
        return comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TaskCommentDTO> pageTaskComments(Long taskId, int pageNum, int pageSize) {
        Page<TaskComment> page = new Page<>(pageNum, pageSize);
        Page<TaskComment> commentPage = this.baseMapper.pageTaskComments(page, taskId);

        Page<TaskCommentDTO> dtoPage = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        List<TaskCommentDTO> dtoList = commentPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    @Override
    public TaskCommentDTO convertToDTO(TaskComment comment) {
        if (comment == null) {
            return null;
        }

        TaskCommentDTO dto = new TaskCommentDTO();
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