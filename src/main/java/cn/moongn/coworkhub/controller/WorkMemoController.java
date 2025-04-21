package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.WorkMemo;
import cn.moongn.coworkhub.model.dto.WorkMemoDTO;
import cn.moongn.coworkhub.service.WorkMemoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/work_memo")
@RequiredArgsConstructor
public class WorkMemoController {

    private final WorkMemoService workMemoService;

    /**
     * 获取当前用户的所有备忘录
     */
    @GetMapping("/list")
    public Result<List<WorkMemoDTO>> getWorkMemoList() {
        try {
            List<WorkMemoDTO> memoList = workMemoService.getWorkMemosByCurrentUser();
            return Result.success(memoList);
        } catch (Exception e) {
            log.error("获取备忘录列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取备忘录详情
     */
    @GetMapping("/{id}")
    public Result<WorkMemoDTO> getWorkMemoDetail(@PathVariable Long id) {
        try {
            WorkMemoDTO memo = workMemoService.getWorkMemoById(id);
            return Result.success(memo);
        } catch (Exception e) {
            log.error("获取备忘录详情失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建备忘录
     */
    @PostMapping("/create")
    public Result<Void> createWorkMemo(@RequestBody WorkMemo workMemo) {
        try {
            boolean success = workMemoService.createWorkMemo(workMemo);
            if (success) {
                return Result.success();
            } else {
                return Result.error("创建备忘录失败");
            }
        } catch (Exception e) {
            log.error("创建备忘录失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新备忘录
     */
    @PutMapping("/update")
    public Result<Void> updateWorkMemo(@RequestBody WorkMemo workMemo) {
        try {
            boolean success = workMemoService.updateWorkMemo(workMemo);
            if (success) {
                return Result.success();
            } else {
                return Result.error("更新备忘录失败");
            }
        } catch (Exception e) {
            log.error("更新备忘录失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除备忘录
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteWorkMemo(@PathVariable Long id) {
        try {
            boolean success = workMemoService.deleteWorkMemo(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除备忘录失败");
            }
        } catch (Exception e) {
            log.error("删除备忘录失败", e);
            return Result.error(e.getMessage());
        }
    }
}