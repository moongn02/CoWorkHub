package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.mapper.TaskActivityMapper;
import cn.moongn.coworkhub.model.TaskActivity;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.TaskActivityDTO;
import cn.moongn.coworkhub.service.TaskActivityService;
import cn.moongn.coworkhub.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskActivityServiceImpl extends ServiceImpl<TaskActivityMapper, TaskActivity> implements TaskActivityService {

    private final UserService userService;

    @Override
    @Transactional
    public boolean add(TaskActivity activity) {
        if (activity.getCreateTime() == null) {
            activity.setCreateTime(LocalDateTime.now());
        }
        if (activity.getUpdateTime() == null) {
            activity.setUpdateTime(LocalDateTime.now());
        }
        return this.save(activity);
    }

    @Override
    public List<TaskActivityDTO> getActivitiesByTaskId(Long taskId) {
        List<TaskActivity> activities = this.baseMapper.selectByTaskId(taskId);
        return activities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TaskActivityDTO convertToDTO(TaskActivity activity) {
        if (activity == null) {
            return null;
        }

        TaskActivityDTO dto = new TaskActivityDTO();
        BeanUtils.copyProperties(activity, dto);

        // 设置操作人姓名
        if (activity.getOperatorId() != null) {
            User operator = userService.getById(activity.getOperatorId());
            if (operator != null) {
                dto.setOperatorName(operator.getRealName());
            }
        }

        return dto;
    }
}