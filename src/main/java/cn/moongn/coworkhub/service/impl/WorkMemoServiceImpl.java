package cn.moongn.coworkhub.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.moongn.coworkhub.common.exception.ApiException;
import cn.moongn.coworkhub.mapper.WorkMemoMapper;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.WorkMemo;
import cn.moongn.coworkhub.model.dto.WorkMemoDTO;
import cn.moongn.coworkhub.service.UserService;
import cn.moongn.coworkhub.service.WorkMemoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkMemoServiceImpl extends ServiceImpl<WorkMemoMapper, WorkMemo> implements WorkMemoService {

    private final UserService userService;

    @Override
    public List<WorkMemoDTO> getWorkMemosByCurrentUser() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new ApiException("用户未登录");
        }

        LambdaQueryWrapper<WorkMemo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WorkMemo::getUserId, currentUser.getId())
                .orderByDesc(WorkMemo::getMemoDate);

        List<WorkMemo> memoList = this.list(queryWrapper);
        return memoList.stream()
                .map(memo -> BeanUtil.copyProperties(memo, WorkMemoDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public WorkMemoDTO getWorkMemoById(Long id) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new ApiException("用户未登录");
        }

        LambdaQueryWrapper<WorkMemo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WorkMemo::getId, id)
                .eq(WorkMemo::getUserId, currentUser.getId());

        WorkMemo memo = this.getOne(queryWrapper);
        if (memo == null) {
            throw new ApiException("备忘录不存在");
        }

        return BeanUtil.copyProperties(memo, WorkMemoDTO.class);
    }

    @Override
    public boolean createWorkMemo(WorkMemo workMemo) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new ApiException("用户未登录");
        }

        workMemo.setUserId(currentUser.getId());
        workMemo.setMemoDate(LocalDate.now());
        return this.save(workMemo);
    }

    @Override
    public boolean updateWorkMemo(WorkMemo workMemo) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new ApiException("用户未登录");
        }

        // 验证备忘录是否属于当前用户
        WorkMemo existingMemo = this.getById(workMemo.getId());
        if (existingMemo == null || !existingMemo.getUserId().equals(currentUser.getId())) {
            throw new ApiException("无权限修改此备忘录");
        }

        workMemo.setMemoDate(LocalDate.now());
        return this.updateById(workMemo);
    }

    @Override
    public boolean deleteWorkMemo(Long id) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new ApiException("用户未登录");
        }

        // 验证备忘录是否属于当前用户
        LambdaQueryWrapper<WorkMemo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WorkMemo::getId, id)
                .eq(WorkMemo::getUserId, currentUser.getId());

        WorkMemo memo = this.getOne(queryWrapper);
        if (memo == null) {
            throw new ApiException("备忘录不存在或无权限删除");
        }

        return this.removeById(id);
    }
}