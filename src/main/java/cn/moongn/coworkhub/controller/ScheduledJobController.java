package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.dto.ScheduledJobDTO;
import cn.moongn.coworkhub.service.ScheduledJobService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时任务管理控制器
 */
@RestController
@RequestMapping("/api/scheduled_job")
@RequiredArgsConstructor
public class ScheduledJobController {

    private final ScheduledJobService jobService;

    /**
     * 分页查询定时任务
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> getJobsByPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) String triggerType) {

        IPage<ScheduledJobDTO> page = jobService.getJobsByPage(current, size, name, status, objectType, triggerType);

        Map<String, Object> data = new HashMap<>();
        data.put("records", page.getRecords());
        data.put("total", page.getTotal());

        return Result.success(data);
    }

    /**
     * 获取定时任务详情
     */
    @GetMapping("/{id}")
    public Result<ScheduledJobDTO> getJobDetail(@PathVariable Long id) {
        ScheduledJobDTO job = jobService.getJobDetail(id);
        if (job == null) {
            return Result.error("任务不存在");
        }
        return Result.success(job);
    }

    /**
     * 添加定时任务
     */
    @PostMapping
    public Result<Long> addJob(@RequestBody ScheduledJobDTO jobDTO) {
        Long id = jobService.addJob(jobDTO);
        return Result.success(id);
    }

    /**
     * 更新定时任务
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateJob(@PathVariable Long id, @RequestBody ScheduledJobDTO jobDTO) {
        jobDTO.setId(id);
        boolean result = jobService.updateJob(jobDTO);
        return result ? Result.success(true) : Result.error("更新失败或任务不存在");
    }

    /**
     * 删除定时任务
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteJob(@PathVariable Long id) {
        boolean result = jobService.deleteJob(id);
        return result ? Result.success(true) : Result.error("删除失败或任务不存在");
    }

    /**
     * 批量删除定时任务
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteJobs(@RequestBody Map<String, List<Long>> requestMap) {
        List<Long> ids = requestMap.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.error("未提供任务ID");
        }

        boolean result = jobService.batchDeleteJobs(ids);
        return result ? Result.success(true) : Result.error("批量删除失败");
    }

    /**
     * 暂停定时任务
     */
    @PutMapping("/pause/{id}")
    public Result<Boolean> pauseJob(@PathVariable Long id) {
        boolean result = jobService.pauseJob(id);
        return result ? Result.success(true) : Result.error("暂停失败或任务不存在");
    }

    /**
     * 恢复定时任务
     */
    @PutMapping("/resume/{id}")
    public Result<Boolean> resumeJob(@PathVariable Long id) {
        boolean result = jobService.resumeJob(id);
        return result ? Result.success(true) : Result.error("恢复失败或任务不存在");
    }

    /**
     * 立即执行一次任务
     */
    @PutMapping("/trigger/{id}")
    public Result<Boolean> triggerJob(@PathVariable Long id) {
        boolean result = jobService.triggerJob(id);
        return result ? Result.success(true) : Result.error("触发失败或任务不存在");
    }
}