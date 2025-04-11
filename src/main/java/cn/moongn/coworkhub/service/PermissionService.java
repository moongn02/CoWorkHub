package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.Permission;
import cn.moongn.coworkhub.model.dto.PermissionDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface PermissionService extends IService<Permission> {

    /**
     * 分页查询权限列表
     * @param current 当前页
     * @param size 每页大小
     * @param params 查询参数
     * @return 分页结果
     */
    Page<PermissionDTO> pagePermissions(int current, int size, Map<String, Object> params);

    /**
     * 根据ID获取权限详情
     * @param id 权限ID
     * @return 权限详情
     */
    PermissionDTO getPermissionById(Long id);

    /**
     * 添加权限
     * @param permission 权限信息
     * @return 是否成功
     */
    boolean addPermission(Permission permission);

    /**
     * 更新权限
     * @param permission 权限信息
     * @return 是否成功
     */
    boolean updatePermission(Permission permission);

    /**
     * 更新权限状态
     * @param id 权限ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updatePermissionStatus(Long id, Integer status);

    /**
     * 删除权限
     * @param id 权限ID
     * @return 是否成功
     */
    boolean deletePermission(Long id);

    /**
     * 获取父级权限列表（用于下拉选择）
     * @return 父级权限列表
     */
    List<PermissionDTO> getParentPermissions();

    /**
     * 批量删除权限
     * @param ids 权限ID列表
     * @return 是否成功
     */
    boolean batchDeletePermissions(List<Long> ids);

    /**
     * 获取权限树
     * @return 权限树
     */
    List<Permission> getPermissionTree();

    /**
     * 根据权限IDs获取权限树
     * @return 权限树
     */
    List<Permission> getPermissionTreeByIds(List<Long> ids);

    /**
     * 过滤出叶子节点权限ID（子权限）
     * @param allPermissionIds 所有选中的权限ID
     * @return 叶子节点权限ID
     */
    List<Long> filterLeafPermissions(List<Long> allPermissionIds);
}
