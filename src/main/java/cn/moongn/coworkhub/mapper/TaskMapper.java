package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.Task;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    // 根据父任务ID查询子任务
    @Select("SELECT * FROM task WHERE parent_task_id = #{parentTaskId}")
    List<Task> selectByParentTaskId(Long parentTaskId);
}