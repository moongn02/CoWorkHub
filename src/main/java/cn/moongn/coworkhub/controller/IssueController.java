package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.Issue;
import cn.moongn.coworkhub.model.IssueComment;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.IssueCommentDTO;
import cn.moongn.coworkhub.service.IssueCommentService;
import cn.moongn.coworkhub.service.IssueService;
import cn.moongn.coworkhub.service.UserService;
import cn.moongn.coworkhub.model.dto.IssueDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/issue")
@RequiredArgsConstructor
@Slf4j
public class IssueController {

    private final UserService userService;
    private final IssueService issueService;
    private final IssueCommentService issueCommentService;

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
     * 更新问题状态
     */
    @PutMapping("/update_status/{id}")
    public Result<Boolean> updateIssueStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            Integer status = null;
            if (params.get("status") != null) {
                status = Integer.valueOf(params.get("status").toString());
            } else {
                return Result.error("状态不能为空");
            }

            String comment = (String) params.get("comment");

            BigDecimal workHours = BigDecimal.ZERO; // 默认为0
            if (params.containsKey("workHours") && params.get("workHours") != null) {
                workHours = new BigDecimal(params.get("workHours").toString());
            }

            // 更新问题状态
            Issue issue = issueService.getById(id);
            if (issue == null) {
                return Result.error("问题不存在");
            }

            issue.setStatus(status);
            boolean success = issueService.updateById(issue);

            // 添加备注和工时
            if (success && comment != null && !comment.isEmpty()) {
                IssueComment issueComment = new IssueComment();
                issueComment.setIssueId(id);
                issueComment.setContent(comment);

                User currentUser = userService.getCurrentUser();
                if (currentUser == null) {
                    return Result.error("系统错误，请联系管理员");
                }
                Long currentUserId = currentUser.getId();
                issueComment.setCreatorId(currentUserId);

                issueComment.setWorkHours(workHours);
                issueCommentService.addIssueComment(issueComment);
            }

            return Result.success(success);
        } catch (Exception e) {
            return Result.error("更新问题状态失败: " + e.getMessage());
        }
    }

    /**
     * 转派问题
     */
    @PutMapping("/transfer/{id}")
    public Result<Boolean> transferIssue(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            Long handlerId = null;
            if (params.get("handlerId") != null) {
                handlerId = Long.valueOf(params.get("handlerId").toString());
            } else {
                return Result.error("处理人不能为空");
            }

            String comment = (String) params.get("comment");

            BigDecimal workHours = BigDecimal.ZERO; // 默认为0
            if (params.containsKey("workHours") && params.get("workHours") != null) {
                workHours = new BigDecimal(params.get("workHours").toString());
            }

            // 更新问题处理人
            Issue issue = issueService.getById(id);
            if (issue == null) {
                return Result.error("问题不存在");
            }

            issue.setHandlerId(handlerId);
            boolean success = issueService.updateById(issue);

            // 添加备注和工时
            if (success && comment != null && !comment.isEmpty()) {
                IssueComment issueComment = new IssueComment();
                issueComment.setIssueId(id);
                issueComment.setContent(comment);

                User currentUser = userService.getCurrentUser();
                if (currentUser == null) {
                    return Result.error("系统错误，请联系管理员");
                }
                Long currentUserId = currentUser.getId();
                issueComment.setCreatorId(currentUserId);

                issueComment.setWorkHours(workHours);
                issueCommentService.addIssueComment(issueComment);
            }

            return Result.success(success);
        } catch (Exception e) {
            return Result.error("转派问题失败: " + e.getMessage());
        }
    }

    /**
     * 修改期望完成时间
     */
    @PutMapping("/expected_time/{id}")
    public Result<Boolean> updateExpectedTime(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            String expectedTime = (String) params.get("expectedTime");
            if (expectedTime != null && expectedTime.contains("/")) {
                expectedTime = expectedTime.replace("/", "-");
            }

            String comment = (String) params.get("comment");

            BigDecimal workHours = BigDecimal.ZERO; // 默认为0
            if (params.containsKey("workHours") && params.get("workHours") != null) {
                workHours = new BigDecimal(params.get("workHours").toString());
            }

            // 更新问题期望完成时间
            Issue issue = issueService.getById(id);
            if (issue == null) {
                return Result.error("问题不存在");
            }

            // 将字符串日期转换为LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(expectedTime, formatter);
            issue.setExpectedTime(dateTime);

            boolean success = issueService.updateById(issue);

            // 添加备注和工时
            if (success && comment != null && !comment.isEmpty()) {
                IssueComment issueComment = new IssueComment();
                issueComment.setIssueId(id);
                issueComment.setContent(comment);

                User currentUser = userService.getCurrentUser();
                if (currentUser == null) {
                    return Result.error("系统错误，请联系管理员");
                }
                Long currentUserId = currentUser.getId();
                issueComment.setCreatorId(currentUserId);

                issueComment.setWorkHours(workHours);
                issueCommentService.addIssueComment(issueComment);
            }

            return Result.success(success);
        } catch (Exception e) {
            return Result.error("修改期望完成时间失败: " + e.getMessage());
        }
    }

    /**
     * 添加问题备注
     */
    @PostMapping("/comment/{id}")
    public Result<IssueCommentDTO> addIssueComment(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            String content = (String) params.get("content");
            if (content == null || content.isEmpty()) {
                return Result.error("备注内容不能为空");
            }

            BigDecimal workHours = BigDecimal.ZERO; // 默认为0
            if (params.containsKey("workHours") && params.get("workHours") != null) {
                workHours = new BigDecimal(params.get("workHours").toString());
            }

            // 创建问题备注
            IssueComment issueComment = new IssueComment();
            issueComment.setIssueId(id);
            issueComment.setContent(content);

            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return Result.error("系统错误，请联系管理员");
            }
            Long currentUserId = currentUser.getId();
            issueComment.setCreatorId(currentUserId);

            issueComment.setWorkHours(workHours);

            boolean success = issueCommentService.addIssueComment(issueComment);
            if (success) {
                IssueCommentDTO dto = issueCommentService.convertToDTO(issueComment);
                return Result.success(dto);
            } else {
                return Result.error("添加备注失败");
            }
        } catch (Exception e) {
            return Result.error("添加问题备注失败: " + e.getMessage());
        }
    }

    /**
     * 获取问题备注列表
     */
    @GetMapping("/comments/{id}")
    public Result<List<IssueCommentDTO>> getIssueComments(@PathVariable Long id) {
        List<IssueCommentDTO> comments = issueCommentService.getIssueComments(id);
        return Result.success(comments);
    }

    /**
     * 分页获取问题备注
     */
    @GetMapping("/comments/page/{id}")
    public Result<Page<IssueCommentDTO>> pageIssueComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<IssueCommentDTO> page = issueCommentService.pageIssueComments(id, pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("分页获取问题备注失败: " + e.getMessage());
        }
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
}