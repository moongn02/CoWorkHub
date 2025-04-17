package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.TaskActivity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TaskActivityMapper extends BaseMapper<TaskActivity> {

    @Select("SELECT * FROM task_activity WHERE task_id = #{taskId} ORDER BY create_time DESC")
    List<TaskActivity> selectByTaskId(Long taskId);
}
