package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.common.exception.ApiException;
import cn.moongn.coworkhub.common.constant.enums.Gender;
import cn.moongn.coworkhub.mapper.RoleMapper;
import cn.moongn.coworkhub.mapper.UserMapper;
import cn.moongn.coworkhub.model.Role;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.UserDTO;
import cn.moongn.coworkhub.model.vo.ResetPasswordVO;
import cn.moongn.coworkhub.service.DepartmentService;
import cn.moongn.coworkhub.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentService departmentService;
    private final RoleMapper roleMapper;

    // 默认密码
    private static final String DEFAULT_PASSWORD = "123456";

    @Override
    public User getById(Long id) {
        return userMapper.getById(id);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return getByUsername(username);
        }
        return null;
    }

    @Override
    public UserDTO formatUser(User user) {
        UserDTO userFormat = new UserDTO();

        BeanUtils.copyProperties(user, userFormat);

        // 密码置空
        userFormat.setPassword(null);

        // 性别转换
        userFormat.setGenderText(Gender.getDescriptionByCode(user.getGender()));

        // 部门名称转换
        userFormat.setDeptText(departmentService.getDepartmentName(user.getDeptId()));

        // 直接上级获取
        userFormat.setSupervisor(departmentService.getSupervisorName(user.getDeptId()));

        // 角色名获取
        userFormat.setRoleName(roleMapper.getById(user.getRoleId()).getName());

        return userFormat;
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.getByUsername(username);
    }

    @Override
    public void update(User user) {
        // 检查用户名是否已存在
        User existUser = getByUsername(user.getUsername());
        if (existUser != null) {
            throw new ApiException("用户名已存在");
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId,user.getId());
        int res = userMapper.update(user, queryWrapper);
        if (res == 0) {
            throw new ApiException("更新失败，请联系系统管理员");
        }
    }

    @Override
    public void changePassword(ResetPasswordVO resetPasswordVO) {
        User user = getCurrentUser();
        if (user != null && passwordEncoder.matches(resetPasswordVO.getCurrentPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(resetPasswordVO.getNewPassword()));
        } else {
            throw new ApiException("当前密码错误");
        }

        update(user);
    }

    @Override
    public List<UserDTO> getUserList() {
        List<User> userList = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getStatus, 1));
        return userList.stream()
                .filter(Objects::nonNull)
                .map(this::formatUser)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public User getUserByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        return this.getOne(queryWrapper);
    }

    @Override
    public Page<UserDTO> pageUsers(int current, int size, Map<String, Object> params) {
        // 创建分页对象
        Page<User> page = new Page<>(current, size);

        // 获取查询参数
        String keyword = params.get("keyword") != null ? params.get("keyword").toString() : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        Long deptId = params.get("deptId") != null ? Long.parseLong(params.get("deptId").toString()) : null;
        Integer roleId = params.get("roleId") != null ? Integer.parseInt(params.get("roleId").toString()) : null;

        // 执行分页查询
        Page<User> userPage = userMapper.selectUserPage(page, keyword, status, deptId, roleId);

        // 转换为DTO
        Page<UserDTO> dtoPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());

        List<UserDTO> records = userPage.getRecords().stream()
                .map(this::formatUser)
                .collect(Collectors.toList());

        dtoPage.setRecords(records);
        return dtoPage;
    }

    @Override
    public UserDTO getUserDetail(Long id) {
        User user = userMapper.getById(id);
        if (user == null) {
            return null;
        }

        return formatUser(user);
    }

    @Override
    @Transactional
    public boolean addUser(User user) {
        // 检查用户名是否已存在
        User existUser = getByUsername(user.getUsername());
        if (existUser != null) {
            throw new ApiException("用户名已存在");
        }

        // 设置默认密码
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));

        // 设置默认状态为启用
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        return save(user);
    }

    @Override
    @Transactional
    public boolean updateUser(User user) {
        // 检查用户名是否已存在
        User existUser = getByUsername(user.getUsername());
        if (existUser != null && !existUser.getId().equals(user.getId())) {
            throw new ApiException("用户名已存在");
        }

        // 不更新密码字段
        user.setPassword(null);

        return updateById(user);
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new ApiException("用户不存在");
        }

        // 检查是否为当前登录用户
        User currentUser = getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(id)) {
            throw new ApiException("不能删除当前登录用户");
        }

        return removeById(id);
    }

    @Override
    @Transactional
    public boolean batchDeleteUsers(List<Long> ids) {
        // 检查是否包含当前登录用户
        User currentUser = getCurrentUser();
        if (currentUser != null && ids.contains(currentUser.getId())) {
            throw new ApiException("不能删除当前登录用户");
        }

        return removeBatchByIds(ids);
    }

    @Override
    @Transactional
    public boolean resetUserPassword(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new ApiException("用户不存在");
        }

        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        return updateById(user);
    }

    @Override
    @Transactional
    public boolean updateUserStatus(Long id, Integer status) {
        User user = getById(id);
        if (user == null) {
            throw new ApiException("用户不存在");
        }

        // 检查是否为当前登录用户
        User currentUser = getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(id) && status == 0) {
            throw new ApiException("不能禁用当前登录用户");
        }

        user.setStatus(status);
        return updateById(user);
    }

    @Override
    @Transactional
    public boolean updateUserRole(Long userId, Long roleId) {
        User user = getById(userId);
        if (user == null) {
            throw new ApiException("用户不存在");
        }

        // 验证角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new ApiException("角色不存在");
        }

        user.setRoleId(roleId);
        return updateById(user);
    }
}