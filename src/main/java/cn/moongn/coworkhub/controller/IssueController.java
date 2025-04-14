package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.Issue;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.service.IssueService;
import cn.moongn.coworkhub.service.UserService;
import cn.moongn.coworkhub.model.dto.IssueDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/issue")
@RequiredArgsConstructor
@Slf4j
public class IssueController {

    private final UserService userService;
    private final IssueService issueService;

    /**
     * 创建问题
     */
    @PostMapping("/create")
    public Result<IssueDTO> createIssue(@RequestBody Issue issue) {
        // 设置创建者为当前登录用户
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return Result.error("系统错误，请联系管理员");
        }
        Long currentUserId = currentUser.getId();
        issue.setCreatorId(currentUserId);

        // 确保状态为已分派
        if (issue.getStatus() == null) {
            issue.setStatus(1);
        }

        try {
            boolean success = issueService.createIssue(issue);
            if (success) {
                Issue savedIssue = issueService.getById(issue.getId());
                IssueDTO issueDTO = issueService.convertToDTO(savedIssue);
                return Result.success(issueDTO);
            } else {
                return Result.error("创建问题失败");
            }
        } catch (Exception e) {
            return Result.error("创建问题失败: " + e.getMessage());
        }
    }

    /**
     * 获取问题详情
     */
    @GetMapping("/{id}")
    public Result<IssueDTO> getIssueDetail(@PathVariable Long id) {
        try {
            Issue issue = issueService.getById(id);
            if (issue == null) {
                return Result.error("问题不存在");
            }

            IssueDTO issueDTO = issueService.convertToDTO(issue);
            return Result.success(issueDTO);
        } catch (Exception e) {
            return Result.error("获取问题详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新问题
     */
    @PutMapping("/update")
    public Result<Boolean> updateIssue(@RequestBody Issue issue) {
        boolean success = issueService.updateById(issue);

        return success ? Result.success(true) : Result.error("更新问题失败");
    }

    /**
     * 分页查询问题
     */
    @GetMapping("/page_list")
    public Result<Page<IssueDTO>> pageIssues(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String taskId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) Long testerId,
            @RequestParam(required = false) Long handlerId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer bugCause,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer severity,
            @RequestParam(required = false) Integer urgency,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("taskId", taskId);
        params.put("title", title);
        params.put("creatorId", creatorId);
        params.put("testerId", testerId);
        params.put("handlerId", handlerId);
        params.put("projectId", projectId);
        params.put("departmentId", departmentId);
        params.put("type", type);
        params.put("bugCause", bugCause);
        params.put("status", status);
        params.put("severity", severity);
        params.put("urgency", urgency);
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        Page<IssueDTO> page = issueService.pageIssues(pageNum, pageSize, params);
        return Result.success(page);
    }

    /**
     * 删除问题
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteIssue(@PathVariable Long id) {
        boolean success = issueService.removeById(id);

        return success ? Result.success(true) : Result.error("删除问题失败");
    }
}