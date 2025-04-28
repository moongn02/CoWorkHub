package cn.moongn.coworkhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.moongn.coworkhub.model.ScheduledJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 定时作业Mapper接口
 */
@Mapper
public interface ScheduledJobMapper extends BaseMapper<ScheduledJob> {
    /**
     * 分页查询定时作业
     * @param page 分页参数
     * @param name 作业名称关键字
     * @param status 作业状态
     * @param objectType 对象类型
     * @param triggerType 触发类型
     * @return 分页数据
     */
    Page<ScheduledJob> selectJobPage(Page<ScheduledJob> page,
                                     @Param("name") String name,
                                     @Param("status") Integer status,
                                     @Param("objectType") String objectType,
                                     @Param("triggerType") String triggerType);
}