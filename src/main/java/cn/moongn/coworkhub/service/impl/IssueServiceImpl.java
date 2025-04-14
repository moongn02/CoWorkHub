package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.mapper.IssueMapper;
import cn.moongn.coworkhub.model.Department;
import cn.moongn.coworkhub.model.Issue;
import cn.moongn.coworkhub.model.Project;
import cn.moongn.coworkhub.model.Task;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.service.DepartmentService;
import cn.moongn.coworkhub.service.IssueService;
import cn.moongn.coworkhub.service.ProjectService;
import cn.moongn.coworkhub.service.TaskService;
import cn.moongn.coworkhub.service.UserService;
import cn.moongn.coworkhub.model.dto.IssueDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueServiceImpl extends ServiceImpl<IssueMapper, Issue> implements IssueService {

    private final UserService userService;
    private final ProjectService projectService;
    private final DepartmentService departmentService;
    private final TaskService taskService;

    @Override
    @Transactional
    public boolean createIssue(Issue issue) {
        return this.save(issue);
    }

    @Override
    public IssueDTO convertToDTO(Issue issue) {
        if (issue == null) {
            return null;
        }

        IssueDTO dto = new IssueDTO();
        BeanUtils.copyProperties(issue, dto);

        // 设置创建人
        if (issue.getCreatorId() != null) {
            User creator = userService.getById(issue.getCreatorId());
            dto.setCreatorName(creator != null ? creator.getRealName() : "未知用户");
        }

        // 设置测试人
        if (issue.getTesterId() != null) {
            User tester = userService.getById(issue.getTesterId());
            dto.setTesterName(tester != null ? tester.getRealName() : "未知用户");
        }

        // 设置处理人
        if (issue.getHandlerId() != null) {
            User handler = userService.getById(issue.getHandlerId());
            dto.setHandlerName(handler != null ? handler.getRealName() : "未知用户");
        }

        // 设置项目
        if (issue.getProjectId() != null) {
            Project project = projectService.getById(issue.getProjectId());
            dto.setProjectName(project != null ? project.getName() : "未知项目");
        }

        // 设置部门
        if (issue.getDepartmentId() != null) {
            Department dept = departmentService.getById(issue.getDepartmentId());
            dto.setDepartmentName(dept != null ? dept.getName() : "未知部门");
        }

        // 设置任务
        if (issue.getTaskId() != null && issue.getTaskId() > 0) {
            Task task = taskService.getById(issue.getTaskId());
            dto.setTaskTitle(task != null ? task.getTitle() : "未知任务");
        }

        // 设置问题类型文本
        if (issue.getType() != null) {
            switch (issue.getType()) {
                case 1: dto.setTypeText("Bug"); break;
                case 2: dto.setTypeText("需求不明确"); break;
                case 3: dto.setTypeText("UI"); break;
                case 4: dto.setTypeText("建议"); break;
                default: dto.setTypeText("未知");
            }
        }

        // 设置Bug原因文本
        if (issue.getBugCause() != null) {
            switch (issue.getBugCause()) {
                case 1: dto.setBugCauseText("代码错误"); break;
                case 2: dto.setBugCauseText("未实现需求"); break;
                case 3: dto.setBugCauseText("测试遗漏"); break;
                case 4: dto.setBugCauseText("历史遗留问题"); break;
                case 5: dto.setBugCauseText("兼容适配"); break;
                case 6: dto.setBugCauseText("其他"); break;
                default: dto.setBugCauseText("未知");
            }
        }

        // 设置状态文本
        if (issue.getStatus() != null) {
            switch (issue.getStatus()) {
                case 1: dto.setStatusText("已分派"); break;
                case 2: dto.setStatusText("处理中"); break;
                case 3: dto.setStatusText("已解决"); break;
                case 4: dto.setStatusText("已暂停"); break;
                case 5: dto.setStatusText("已关闭"); break;
                default: dto.setStatusText("未知");
            }
        }

        // 设置严重程度文本
        if (issue.getSeverity() != null) {
            switch (issue.getSeverity()) {
                case 1: dto.setSeverityText("致命错误【1级】"); break;
                case 2: dto.setSeverityText("严重错误【2级】"); break;
                case 3: dto.setSeverityText("一般错误【3级】"); break;
                case 4: dto.setSeverityText("细微错误【4级】"); break;
                case 5: dto.setSeverityText("改进错误【5级】"); break;
                default: dto.setSeverityText("未知");
            }
        }

        // 设置紧急程度文本
        if (issue.getUrgency() != null) {
            switch (issue.getUrgency()) {
                case 0: dto.setUrgencyText("一般"); break;
                case 1: dto.setUrgencyText("紧急"); break;
                default: dto.setUrgencyText("未知");
            }
        }

        // 设置浏览器文本
        if (issue.getBrowser() != null) {
            switch (issue.getBrowser()) {
                case 1: dto.setBrowserText("IE"); break;
                case 2: dto.setBrowserText("Microsoft Edge"); break;
                case 3: dto.setBrowserText("Chrome"); break;
                case 4: dto.setBrowserText("Firefox"); break;
                case 5: dto.setBrowserText("Safari"); break;
                case 6: dto.setBrowserText("其他"); break;
                default: dto.setBrowserText("未知");
            }
        }

        // 设置平台文本
        if (issue.getPlatform() != null) {
            switch (issue.getPlatform()) {
                case 1: dto.setPlatformText("PC"); break;
                case 2: dto.setPlatformText("移动设备"); break;
                default: dto.setPlatformText("未知");
            }
        }

        // 设置系统文本
        if (issue.getSys() != null) {
            if (issue.getPlatform() != null && issue.getPlatform() == 1) {
                // PC
                switch (issue.getSys()) {
                    case 1: dto.setSysText("Windows 7"); break;
                    case 2: dto.setSysText("Windows 8"); break;
                    case 3: dto.setSysText("Windows 10+"); break;
                    case 4: dto.setSysText("Mac OS"); break;
                    case 5: dto.setSysText("其他"); break;
                    default: dto.setSysText("未知");
                }
            } else {
                // 移动设备
                switch (issue.getSys()) {
                    case 1: dto.setSysText("Android"); break;
                    case 2: dto.setSysText("iOS"); break;
                    case 5: dto.setSysText("其他"); break;
                    default: dto.setSysText("未知");
                }
            }
        }

        return dto;
    }

    @Override
    public Page<IssueDTO> pageIssues(Integer pageNum, Integer pageSize, Map<String, Object> params) {
        Page<Issue> page = new Page<>(pageNum, pageSize);

        // 构建条件
        LambdaQueryWrapper<Issue> queryWrapper = new LambdaQueryWrapper<>();

        // 添加ID搜索
        String id = (String) params.get("id");
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.eq(Issue::getId, id);
        }

        // 添加任务ID搜索
        String taskId = (String) params.get("taskId");
        if (StringUtils.isNotBlank(taskId)) {
            queryWrapper.eq(Issue::getTaskId, taskId);
        }

        // 添加标题搜索
        String title = (String) params.get("title");
        if (StringUtils.isNotBlank(title)) {
            queryWrapper.like(Issue::getTitle, title);
        }

        // 添加创建人搜索
        Long creatorId = (Long) params.get("creatorId");
        if (creatorId != null) {
            queryWrapper.eq(Issue::getCreatorId, creatorId);
        }

        // 添加测试人搜索
        Long testerId = (Long) params.get("testerId");
        if (testerId != null) {
            queryWrapper.eq(Issue::getTesterId, testerId);
        }

        // 添加处理人搜索
        Long handlerId = (Long) params.get("handlerId");
        if (handlerId != null) {
            queryWrapper.eq(Issue::getHandlerId, handlerId);
        }

        // 添加项目搜索
        Long projectId = (Long) params.get("projectId");
        if (projectId != null) {
            queryWrapper.eq(Issue::getProjectId, projectId);
        }

        // 添加部门搜索
        Long departmentId = (Long) params.get("departmentId");
        if (departmentId != null) {
            queryWrapper.eq(Issue::getDepartmentId, departmentId);
        }

        // 添加问题类型搜索
        Integer type = (Integer) params.get("type");
        if (type != null) {
            queryWrapper.eq(Issue::getType, type);
        }

        // 添加Bug原因搜索
        Integer bugCause = (Integer) params.get("bugCause");
        if (bugCause != null) {
            queryWrapper.eq(Issue::getBugCause, bugCause);
        }

        // 添加状态搜索
        Integer status = (Integer) params.get("status");
        if (status != null) {
            queryWrapper.eq(Issue::getStatus, status);
        }

        // 添加严重程度搜索
        Integer severity = (Integer) params.get("severity");
        if (severity != null) {
            queryWrapper.eq(Issue::getSeverity, severity);
        }

        // 添加紧急程度搜索
        Integer urgency = (Integer) params.get("urgency");
        if (urgency != null) {
            queryWrapper.eq(Issue::getUrgency, urgency);
        }

        // 添加期望完成时间范围搜索
        String startDate = (String) params.get("startDate");
        String endDate = (String) params.get("endDate");
        if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            queryWrapper.between(Issue::getExpectedTime, startDate + "00:00:00", endDate + " 23:59:59");
        }

        // 添加排序
        queryWrapper.orderByAsc(Issue::getCreateTime);

        // 执行查询
        Page<Issue> resultPage = this.page(page, queryWrapper);

        // 转换为DTO
        Page<IssueDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<IssueDTO> dtoList = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }
}