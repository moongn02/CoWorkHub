package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.dto.ScheduledJobLogDTO;
import cn.moongn.coworkhub.service.ScheduledJobLogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 定时任务日志控制器
 */
@RestController
@RequestMapping("/api/scheduled_job_log")
@RequiredArgsConstructor
public class ScheduledJobLogController {

    private final ScheduledJobLogService logService;

    /**
     * 分页查询任务执行日志
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> getLogsByPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        IPage<ScheduledJobLogDTO> page = logService.getLogsByPage(current, size, jobId, status);

        Map<String, Object> data = new HashMap<>();
        data.put("records", page.getRecords());
        data.put("total", page.getTotal());

        return Result.success(data);
    }

    /**
     * 获取任务执行日志详情
     */
    @GetMapping("/{id}")
    public Result<ScheduledJobLogDTO> getLogDetail(@PathVariable Long id) {
        ScheduledJobLogDTO log = logService.getLogDetail(id);
        if (log == null) {
            return Result.error("日志不存在");
        }
        return Result.success(log);
    }

    /**
     * 清理过期日志
     */
    @DeleteMapping("/clean")
    public Result<Integer> cleanExpiredLogs(@RequestParam(defaultValue = "30") int days) {
        int count = logService.cleanExpiredLogs(days);
        return Result.success(count);
    }
}