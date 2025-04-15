package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.TaskComment;
import cn.moongn.coworkhub.model.dto.TaskCommentDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface TaskCommentService extends IService<TaskComment> {

    /**
     * 添加任务备注
     */
    boolean addTaskComment(TaskComment comment);

    /**
     * 获取任务的所有备注
     */
    List<TaskCommentDTO> getTaskComments(Long taskId);

    /**
     * 分页获取任务备注
     */
    Page<TaskCommentDTO> pageTaskComments(Long taskId, int pageNum, int pageSize);

    /**
     * 将 TaskComment 转换为 DTO
     */
    TaskCommentDTO convertToDTO(TaskComment comment);
}