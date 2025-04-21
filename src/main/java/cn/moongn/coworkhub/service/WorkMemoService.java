package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.WorkMemo;
import cn.moongn.coworkhub.model.dto.WorkMemoDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface WorkMemoService extends IService<WorkMemo> {
    // 获取当前用户的所有备忘录
    List<WorkMemoDTO> getWorkMemosByCurrentUser();

    // 获取备忘录详情
    WorkMemoDTO getWorkMemoById(Long id);

    // 创建备忘录
    boolean createWorkMemo(WorkMemo workMemo);

    // 更新备忘录
    boolean updateWorkMemo(WorkMemo workMemo);

    // 删除备忘录
    boolean deleteWorkMemo(Long id);
}