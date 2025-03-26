package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.mapper.DepartmentMapper;
import cn.moongn.coworkhub.mapper.ProjectMapper;
import cn.moongn.coworkhub.mapper.UserMapper;
import cn.moongn.coworkhub.model.Department;
import cn.moongn.coworkhub.model.Project;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.ProjectDTO;
import cn.moongn.coworkhub.service.ProjectService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    private final ProjectMapper projectMapper;
    private final DepartmentMapper departmentMapper;
    private final UserMapper userMapper;

    @Override
    public Page<ProjectDTO> pageProjects(int current, int size, Map<String, Object> params) {
        // 创建分页对象
        Page<Project> page = new Page<>(current, size);

        // 获取查询参数
        String keyword = params.get("keyword") != null ? params.get("keyword").toString() : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        Long departmentId = params.get("departmentId") != null ? Long.parseLong(params.get("departmentId").toString()) : null;
        Long parentId = params.get("parentId") != null ? Long.parseLong(params.get("parentId").toString()) : null;

        // 执行分页查询
        Page<Project> projectPage = projectMapper.selectProjectPage(page, keyword, status, departmentId, parentId);

        // 转换为DTO
        Page<ProjectDTO> dtoPage = new Page<>(projectPage.getCurrent(), projectPage.getSize(), projectPage.getTotal());

        if (projectPage.getRecords().isEmpty()) {
            dtoPage.setRecords(new ArrayList<>());
            return dtoPage;
        }

        // 转换为DTO
        List<ProjectDTO> dtoList = projectPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public ProjectDTO getProjectDetail(Long id) {
        Project project = projectMapper.getById(id);
        if (project == null) {
            return null;
        }
        return convertToDTO(project);
    }

    @Override
    @Transactional
    public boolean addProject(Project project) {
        // 如果parentId为null，设置为0
        if (project.getParentId() == null) {
            project.setParentId(0L);
        }

        return projectMapper.insert(project) > 0;
    }

    @Override
    @Transactional
    public boolean updateProject(Project project) {
        // 设置更新时间

        // 如果parentId为null，设置为0
        if (project.getParentId() == null) {
            project.setParentId(0L);
        }

        return projectMapper.updateById(project) > 0;
    }

    @Override
    @Transactional
    public boolean updateProjectStatus(Long id, Integer status) {
        Project project = new Project();
        project.setId(id);
        project.setStatus(status);

        return projectMapper.updateById(project) > 0;
    }

    @Override
    public List<ProjectDTO> getParentProjects() {
        // 创建查询条件，查找parent_id为0的项目，且状态为启用
        LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Project::getParentId, 0L)
                .eq(Project::getStatus, 1);

        // 查询数据库
        List<Project> projects = projectMapper.selectList(queryWrapper);

        // 转换为DTO并返回
        return projects.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 将Project实体转换为ProjectDTO
     */
    private ProjectDTO convertToDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        BeanUtils.copyProperties(project, dto);

        // 设置状态文本
        dto.setStatusText(project.getStatus() == 1 ? "启用" : "禁用");

        // 设置更新人名称
        if (project.getUpdaterId() != null) {
            User updater = userMapper.getById(project.getUpdaterId());
            if (updater != null) {
                dto.setUpdaterName(updater.getRealName());
            }
        }

        // 设置部门名称
        if (project.getDepartmentId() != null) {
            Department dept = departmentMapper.getById(project.getDepartmentId());
            if (dept != null) {
                dto.setDepartmentName(dept.getName());
            }
        }

        // 设置上级项目名称
        if (project.getParentId() != null && project.getParentId() > 0) {
            Project parent = projectMapper.getById(project.getParentId());
            if (parent != null) {
                dto.setParentName(parent.getName());
            }
        } else {
            dto.setParentName("-");
        }

        return dto;
    }
}

