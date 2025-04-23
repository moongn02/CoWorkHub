package cn.moongn.coworkhub.controller;

import cn.moongn.coworkhub.common.api.Result;
import cn.moongn.coworkhub.model.Department;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.DeptPersonnelDTO;
import cn.moongn.coworkhub.model.dto.WorkLogDTO;
import cn.moongn.coworkhub.service.DepartmentService;
import cn.moongn.coworkhub.service.UserService;
import cn.moongn.coworkhub.service.WorkLogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamController {

    private final DepartmentService departmentService;
    private final WorkLogService workLogService;
    private final UserService userService;

    /**
     * 获取部门人员树
     * 如果当前用户是父部门管理员，展示父部门及所有子部门的树形结构
     */
    @GetMapping("/personnel_tree")
    public Result<List<DeptPersonnelDTO>> getDepartmentPersonnelTree() {
        try {
            // 获取当前用户
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }

            // 获取所有部门列表
            List<Department> allDepartments = departmentService.list();

            // 找出当前用户管理的部门（即 leaderId 等于用户 ID 的部门）
            List<Department> managedDepartments = allDepartments.stream()
                    .filter(dept -> currentUser.getId().equals(dept.getLeaderId()))
                    .collect(Collectors.toList());

            // 如果用户不是任何部门的管理员，则返回空列表
            if (managedDepartments.isEmpty()) {
                return Result.success(new ArrayList<>());
            }

            // 构建部门的父子关系映射
            Map<Long, List<Department>> childrenMap = buildDepartmentChildrenMap(allDepartments);

            // 找出顶级管理部门（即用户管理的部门中，不是其他管理部门的子部门）
            List<Department> topManagedDepartments = findTopManagedDepartments(managedDepartments);

            // 获取用户列表
            List<User> userList = userService.list();

            // 构建最终的部门-人员树
            List<DeptPersonnelDTO> result = new ArrayList<>();

            for (Department topDept : topManagedDepartments) {
                DeptPersonnelDTO topDeptDTO = buildDeptPersonnelTree(topDept, childrenMap, userList, new HashSet<>());
                result.add(topDeptDTO);
            }

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取部门人员树失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 构建部门的父子关系映射
     */
    private Map<Long, List<Department>> buildDepartmentChildrenMap(List<Department> departments) {
        Map<Long, List<Department>> childrenMap = new HashMap<>();

        for (Department dept : departments) {
            Long parentId = dept.getParentId();
            if (parentId == null) {
                parentId = 0L;
            }

            if (!childrenMap.containsKey(parentId)) {
                childrenMap.put(parentId, new ArrayList<>());
            }
            childrenMap.get(parentId).add(dept);
        }

        return childrenMap;
    }

    /**
     * 找出顶级管理部门（用户管理的部门中不是其他管理部门的子部门）
     */
    private List<Department> findTopManagedDepartments(List<Department> managedDepartments) {
        if (managedDepartments.size() <= 1) {
            return managedDepartments;
        }

        // 收集所有管理部门的ID
        Set<Long> managedDeptIds = managedDepartments.stream()
                .map(Department::getId)
                .collect(Collectors.toSet());

        // 过滤出不是其他管理部门子部门的部门
        return managedDepartments.stream()
                .filter(dept -> !managedDeptIds.contains(dept.getParentId()))
                .collect(Collectors.toList());
    }

    /**
     * 递归构建部门-人员树
     */
    private DeptPersonnelDTO buildDeptPersonnelTree(
            Department dept,
            Map<Long, List<Department>> childrenMap,
            List<User> userList,
            Set<Long> processedDeptIds) {

        // 防止循环引用
        if (processedDeptIds.contains(dept.getId())) {
            return null;
        }
        processedDeptIds.add(dept.getId());

        // 创建部门节点
        DeptPersonnelDTO deptDTO = new DeptPersonnelDTO();
        deptDTO.setId(dept.getId());
        deptDTO.setLabel(dept.getName());
        deptDTO.setChildren(new ArrayList<>());
        deptDTO.setIsLeaf(false);

        // 添加子部门
        List<Department> childDepartments = childrenMap.getOrDefault(dept.getId(), Collections.emptyList());
        for (Department childDept : childDepartments) {
            DeptPersonnelDTO childDeptDTO = buildDeptPersonnelTree(childDept, childrenMap, userList, processedDeptIds);
            if (childDeptDTO != null) {
                deptDTO.getChildren().add(childDeptDTO);
            }
        }

        // 添加部门人员
        for (User user : userList) {
            if (user.getDeptId() != null && user.getDeptId().equals(dept.getId())) {
                DeptPersonnelDTO userDTO = new DeptPersonnelDTO();
                userDTO.setId(user.getId());
                userDTO.setLabel(user.getRealName());
                userDTO.setChildren(null);
                userDTO.setIsLeaf(true);

                deptDTO.getChildren().add(userDTO);
            }
        }

        return deptDTO;
    }

    /**
     * 获取人员工作日志
     */
    @GetMapping("/personnel_logs")
    public Result<Page<WorkLogDTO>> pagePersonnelWorkLogs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String userIds,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer logType) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userIds", userIds);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("logType", logType);

            Page<WorkLogDTO> page = workLogService.pagePersonnelWorkLogs(pageNum, pageSize, params);
            return Result.success(page);
        } catch (Exception e) {
            log.error("获取人员工作日志失败", e);
            return Result.error(e.getMessage());
        }
    }
}