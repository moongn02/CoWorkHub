package cn.moongn.coworkhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.moongn.coworkhub.model.ScheduledJobLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 定时作业日志Mapper接口
 */
@Mapper
public interface ScheduledJobLogMapper extends BaseMapper<ScheduledJobLog> {
    /**
     * 分页查询作业执行日志
     * @param page 分页参数
     * @param jobId 作业ID
     * @param status 执行状态
     * @return 分页数据
     */
    Page<ScheduledJobLog> selectLogPage(Page<ScheduledJobLog> page,
                                         @Param("jobId") Long jobId,
                                         @Param("status") Integer status);
}