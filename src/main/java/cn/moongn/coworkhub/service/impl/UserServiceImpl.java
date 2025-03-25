package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.common.exception.ApiException;
import cn.moongn.coworkhub.constant.enums.Gender;
import cn.moongn.coworkhub.mapper.UserMapper;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.UserDTO;
import cn.moongn.coworkhub.model.vo.ResetPasswordVO;
import cn.moongn.coworkhub.service.DepartmentService;
import cn.moongn.coworkhub.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentService departmentService;

    @Autowired
    public UserServiceImpl(UserMapper userMapper, @Lazy PasswordEncoder passwordEncoder, DepartmentService departmentService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.departmentService = departmentService;
    }

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
        userFormat.setGender(Gender.getDescriptionByCode(user.getGender()));

        // 部门名称转换
        userFormat.setDepartment(departmentService.getDepartmentName(user.getDeptId()));

        // 直接上级获取
        userFormat.setSupervisor(departmentService.getSupervisorName(user.getDeptId()));

        return userFormat;

    }

    @Override
    public User getByUsername(String username) {
        return userMapper.getByUsername(username);
    }

    @Override
    public void save(User user) {
        int res = userMapper.insert(user);
        if (res == 0) {
            throw new ApiException("注册失败，请联系系统管理员");
        }

    }

    @Override
    public void update(User user) {
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
        List<User> userList = userMapper.selectList(null);
        return userList.stream()
                .filter(Objects::nonNull)
                .map(this::formatUser)
                .filter(Objects::nonNull)
                .toList();
    }
}