package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.UserDTO;
import cn.moongn.coworkhub.model.vo.ResetPasswordVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

public interface UserService extends IService<User> {
    User getById(Long id);
    User getCurrentUser();
    UserDTO formatUser(User user);
    User getByUsername(String username);
    void update(User user);
    void changePassword(@Valid @RequestBody ResetPasswordVO resetPasswordVO);
    List<UserDTO> getUserList();

    /**
     * 分页查询用户列表
     */
    Page<UserDTO> pageUsers(int current, int size, Map<String, Object> params);

    /**
     * 获取用户详情
     */
    UserDTO getUserDetail(Long id);

    /**
     * 添加用户
     */
    boolean addUser(User user);

    /**
     * 更新用户
     */
    boolean updateUser(User user);

    /**
     * 删除用户
     */
    boolean deleteUser(Long id);

    /**
     * 批量删除用户
     */
    boolean batchDeleteUsers(List<Long> ids);

    /**
     * 重置用户密码
     */
    boolean resetUserPassword(Long id);

    /**
     * 更新用户状态
     */
    boolean updateUserStatus(Long id, Integer status);

    /**
     * 更新用户角色
     */
    boolean updateUserRole(Long userId, Integer roleId);
}