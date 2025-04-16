package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.mapper.IssueActivityMapper;
import cn.moongn.coworkhub.model.IssueActivity;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.IssueActivityDTO;
import cn.moongn.coworkhub.service.IssueActivityService;
import cn.moongn.coworkhub.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IssueActivityServiceImpl extends ServiceImpl<IssueActivityMapper, IssueActivity> implements IssueActivityService {

    @Resource
    private UserService userService;

    @Override
    @Transactional
    public boolean add(IssueActivity activity) {
        return save(activity);
    }

    @Override
    public List<IssueActivityDTO> getActivitiesByIssueId(Long issueId) {
        List<IssueActivity> activities = baseMapper.selectByIssueId(issueId);

        if (activities.isEmpty()) {
            return new ArrayList<>();
        }

        // 收集所有操作人ID
        List<Long> operatorIds = activities.stream()
                .map(IssueActivity::getOperatorId)
                .distinct()
                .collect(Collectors.toList());

        // 批量获取用户信息
        List<User> users = userService.listByIds(operatorIds);
        Map<Long, String> userIdNameMap = users.stream()
                .collect(Collectors.toMap(User::getId, User::getRealName));

        // 转换为DTO
        return activities.stream().map(activity -> {
            IssueActivityDTO dto = new IssueActivityDTO();
            dto.setId(activity.getId());
            dto.setContent(activity.getContent());
            dto.setIssueId(activity.getIssueId());
            dto.setOperatorId(activity.getOperatorId());
            dto.setOperatorName(userIdNameMap.getOrDefault(activity.getOperatorId(), "未知用户"));
            dto.setCreateTime(activity.getCreateTime());
            return dto;
        }).collect(Collectors.toList());
    }
}