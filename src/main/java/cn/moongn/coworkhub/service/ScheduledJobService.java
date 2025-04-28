package cn.moongn.coworkhub.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.moongn.coworkhub.model.ScheduledJob;
import cn.moongn.coworkhub.model.dto.ScheduledJobDTO;

import java.util.List;

/**
 * 定时任务服务接口
 */
public interface ScheduledJobService extends IService<ScheduledJob> {

    /**
     * 分页查询定时任务
     * @param current 当前页
     * @param size 每页条数
     * @param name 任务名称
     * @param status 任务状态
     * @param objectType 对象类型
     * @param triggerType 触发类型
     * @return 分页结果
     */
    IPage<ScheduledJobDTO> getJobsByPage(long current, long size, String name, Integer status, String objectType, String triggerType);

    /**
     * 添加定时任务
     * @param job 定时任务信息
     * @return 任务ID
     */
    Long addJob(ScheduledJobDTO job);

    /**
     * 更新定时任务
     * @param job 定时任务信息
     * @return 是否成功
     */
    boolean updateJob(ScheduledJobDTO job);

    /**
     * 获取定时任务详情
     * @param id 任务ID
     * @return 任务详情
     */
    ScheduledJobDTO getJobDetail(Long id);

    /**
     * 删除定时任务
     * @param id 任务ID
     * @return 是否成功
     */
    boolean deleteJob(Long id);

    /**
     * 暂停定时任务
     * @param id 任务ID
     * @return 是否成功
     */
    boolean pauseJob(Long id);

    /**
     * 恢复定时任务
     * @param id 任务ID
     * @return 是否成功
     */
    boolean resumeJob(Long id);

    /**
     * 立即执行一次任务
     * @param id 任务ID
     * @return 是否成功
     */
    boolean triggerJob(Long id);

    /**
     * 批量删除定时任务
     * @param ids 任务ID列表
     * @return 是否成功
     */
    boolean batchDeleteJobs(List<Long> ids);
}