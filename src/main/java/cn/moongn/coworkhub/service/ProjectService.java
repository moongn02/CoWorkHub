package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.Project;
import cn.moongn.coworkhub.model.dto.ProjectDTO;
import cn.moongn.coworkhub.model.dto.ProjectTreeDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface ProjectService extends IService<Project> {

    /**
     * 分页查询项目列表
     * @param current 当前页
     * @param size 每页大小
     * @param params 查询参数
     * @return 分页结果
     */
    Page<ProjectDTO> pageProjects(int current, int size, Map<String, Object> params);

    /**
     * 获取项目详情
     * @param id 项目ID
     * @return 项目详情
     */
    ProjectDTO getProjectDetail(Long id);

    /**
     * 添加项目
     * @param project 项目信息
     * @return 是否成功
     */
    boolean addProject(Project project);

    /**
     * 更新项目
     * @param project 项目信息
     * @return 是否成功
     */
    boolean updateProject(Project project);

    /**
     * 删除项目
     * @param id 项目ID
     * @return 是否成功
     */
    boolean deleteProject(Long id);

    /**
     * 更新项目状态
     * @param id 项目ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateProjectStatus(Long id, Integer status);

    /**
     * 获取项目树结构
     * @return 树形结构的项目列表
     */
    List<ProjectTreeDTO> getProjectTree();

    /**
     * 获取父级项目列表（用于下拉选择）
     * @return 父级项目列表
     */
    List<ProjectDTO> getParentProjects();

    /**
     * 批量删除项目
     * @param ids 项目ID列表
     * @return 是否成功
     */
    boolean batchDeleteProjects(List<Long> ids);
}