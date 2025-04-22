package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.WorkLog;
import cn.moongn.coworkhub.model.dto.WorkLogDTO;
import cn.moongn.coworkhub.service.WorkLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/work_log")
@RequiredArgsConstructor
public class WorkLogController {

    private final WorkLogService workLogService;

    /**
     * 获取工作日志列表
     */
    @GetMapping("/list")
    public Result<List<WorkLogDTO>> getWorkLogList(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer type) {
        try {
            List<WorkLogDTO> logList = workLogService.getWorkLogsByCurrentUser(
                    startDate, endDate, year, month, type
            );
            return Result.success(logList);
        } catch (Exception e) {
            log.error("获取工作日志列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取工作日志详情
     */
    @GetMapping("/{id}")
    public Result<WorkLogDTO> getWorkLogDetail(@PathVariable Long id) {
        try {
            WorkLogDTO log = workLogService.getWorkLogById(id);
            return Result.success(log);
        } catch (Exception e) {
            log.error("获取工作日志详情失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建工作日志
     */
    @PostMapping("/create")
    public Result<Void> createWorkLog(@RequestBody WorkLog workLog) {
        try {
            boolean success = workLogService.createWorkLog(workLog);
            if (success) {
                return Result.success();
            } else {
                return Result.error("创建工作日志失败");
            }
        } catch (Exception e) {
            log.error("创建工作日志失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新工作日志
     */
    @PutMapping("/update")
    public Result<Void> updateWorkLog(@RequestBody WorkLog workLog) {
        try {
            boolean success = workLogService.updateWorkLog(workLog);
            if (success) {
                return Result.success();
            } else {
                return Result.error("更新工作日志失败");
            }
        } catch (Exception e) {
            log.error("更新工作日志失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取本月工作日志统计信息
     */
    @GetMapping("/monthly_statistics")
    public Result<Map<String, Object>> getMonthlyStatistics() {
        try {
            Map<String, Object> statistics = workLogService.getMonthlyStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取工作日志统计信息失败", e);
            return Result.error(e.getMessage());
        }
    }
}