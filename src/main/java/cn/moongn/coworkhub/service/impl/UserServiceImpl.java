package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.mapper.UserMapper;
import cn.moongn.coworkhub.model.ChangePasswordRequest;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.UserDTO;
import cn.moongn.coworkhub.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserMapper userMapper, @Lazy PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO getById(Long id) {

        return userMapper.findById(id);
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
    public User getByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public UserDTO save(User user) {
        int res = userMapper.insert(user);
        if (res == 0) {
            throw new RuntimeException("注册失败，请联系系统管理员");
        }

        return getById(user.getId());
    }

    @Override
    public void update(User user) {
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }
        // 保持密码不变
        user.setPassword(existingUser.getPassword());
        // 使用 MyBatis Plus 的更新方法
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        userMapper.update(user, wrapper);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {

    }

//    @Override
//    public void changePassword(ChangePasswordRequest request) {
//        User user = userMapper.findByUsername(request.getUsername());
//        if (user != null && passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
//            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
//            userMapper.updateById(user);
//        } else {
//            throw new RuntimeException("当前密码不正确");
//        }
//    }
}