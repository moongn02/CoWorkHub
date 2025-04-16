package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.IssueActivity;
import cn.moongn.coworkhub.model.dto.IssueActivityDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IssueActivityService extends IService<IssueActivity> {

    /**
     * 添加活动记录
     * @param activity 活动记录
     * @return 是否成功
     */
    boolean add(IssueActivity activity);

    /**
     * 获取问题的活动记录
     * @param issueId 问题ID
     * @return 活动记录列表
     */
    List<IssueActivityDTO> getActivitiesByIssueId(Long issueId);
}