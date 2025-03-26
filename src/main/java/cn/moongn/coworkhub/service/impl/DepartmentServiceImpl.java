package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.mapper.DepartmentMapper;
import cn.moongn.coworkhub.mapper.UserMapper;
import cn.moongn.coworkhub.model.Department;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.DepartmentDTO;
import cn.moongn.coworkhub.model.dto.DepartmentTreeDTO;
import cn.moongn.coworkhub.service.DepartmentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final UserMapper userMapper;

    @Override
    public String getDepartmentName(Long deptId) {
        if (deptId == null || deptId == 0) {
            return "-";
        }
        Department department = departmentMapper.getById(deptId);
        return department != null ? department.getName() : null;
    }

    @Override
    public String getSupervisorName(Long deptId) {
        if (deptId == null) {
            return null;
        }
        Department department = departmentMapper.getById(deptId);
        Long superiorId = department.getLeaderId();
        String superiorRealName = null;
        if (superiorId != null) {
            User superior = userMapper.getById(superiorId);
            superiorRealName = superior.getRealName();
        }
        return superiorRealName;
    }

    @Override
    public Page<DepartmentDTO> pageDepartments(int current, int size, Map<String, Object> params) {
        // 创建分页对象
        Page<Department> page = new Page<>(current, size);

        // 获取查询参数
        String keyword = params.get("keyword") != null ? params.get("keyword").toString() : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        Long parentId = params.get("parentId") != null ? Long.parseLong(params.get("parentId").toString()) : null;

        // 执行分页查询
        Page<Department> departmentPage = departmentMapper.selectDepartmentPage(page, keyword, status, parentId);

        // 转换为DTO
        Page<DepartmentDTO> dtoPage = new Page<>(departmentPage.getCurrent(), departmentPage.getSize(), departmentPage.getTotal());
        List<DepartmentDTO> dtoList = departmentPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public List<DepartmentDTO> getAllDepartments() {
        List<Department> departments = departmentMapper.selectAllDepartments();
        return departments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DepartmentDTO> getParentDepartments() {
        // 创建查询条件，查找parent_id为0的部门
        LambdaQueryWrapper<Department> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Department::getParentId, 0L);

        // 查询数据库
        List<Department> departments = departmentMapper.selectList(queryWrapper);

        // 转换为DTO并返回
        return departments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentDTO getDepartmentDetail(Long id) {
        Department department = departmentMapper.getById(id);
        if (department == null) {
            return null;
        }
        return convertToDTO(department);
    }

    @Override
    public List<DepartmentTreeDTO> getDepartmentTree() {
        // 查询所有部门
        List<Department> departments = departmentMapper.selectAllDepartments();

        // 构建部门树
        List<DepartmentTreeDTO> rootDepartments = new ArrayList<>();

        // 按parentId分组
        Map<Long, List<Department>> departmentMap = departments.stream()
                .collect(Collectors.groupingBy(Department::getParentId));

        // 获取根部门（parentId = 0）
        List<Department> rootDepts = departmentMap.getOrDefault(0L, new ArrayList<>());

        // 递归构建部门树
        for (Department dept : rootDepts) {
            DepartmentTreeDTO treeNode = new DepartmentTreeDTO();
            treeNode.setId(dept.getId());
            treeNode.setName(dept.getName());
            treeNode.setChildren(buildDepartmentTree(dept.getId(), departmentMap));
            rootDepartments.add(treeNode);
        }

        return rootDepartments;
    }

    /**
     * 递归构建部门树
     */
    private List<DepartmentTreeDTO> buildDepartmentTree(Long parentId, Map<Long, List<Department>> departmentMap) {
        List<Department> children = departmentMap.getOrDefault(parentId, new ArrayList<>());
        if (children.isEmpty()) {
            return new ArrayList<>();
        }

        List<DepartmentTreeDTO> childrenTree = new ArrayList<>();
        for (Department dept : children) {
            DepartmentTreeDTO treeNode = new DepartmentTreeDTO();
            treeNode.setId(dept.getId());
            treeNode.setName(dept.getName());
            treeNode.setChildren(buildDepartmentTree(dept.getId(), departmentMap));
            childrenTree.add(treeNode);
        }

        return childrenTree;
    }

    @Override
    @Transactional
    public boolean addDepartment(Department department) {
        return this.save(department);
    }

    @Override
    @Transactional
    public boolean updateDepartment(Department department) {
        return this.updateById(department);
    }

    @Override
    @Transactional
    public boolean updateDepartmentStatus(Long id, Integer status) {
        Department department = new Department();
        department.setId(id);
        department.setStatus(status);
        return this.updateById(department);
    }

    /**
     * 将Department实体转换为DepartmentDTO
     */
    private DepartmentDTO convertToDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        BeanUtils.copyProperties(department, dto);

        // 设置状态文本
        dto.setStatusText(department.getStatus() == 1 ? "启用" : "禁用");

        // 设置部门负责人名称
        if (department.getLeaderId() != null) {
            User leader = userMapper.getById(department.getLeaderId());
            if (leader != null) {
                dto.setLeaderName(leader.getRealName());
            }
        }

        // 设置上级部门名称
        if (department.getParentId() != null && department.getParentId() > 0) {
            Department parent = departmentMapper.getById(department.getParentId());
            if (parent != null) {
                dto.setParentName(parent.getName());
            }
        } else {
            dto.setParentName("-");
        }

        return dto;
    }


}
