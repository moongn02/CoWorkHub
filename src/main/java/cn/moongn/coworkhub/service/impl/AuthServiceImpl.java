 package cn.moongn.coworkhub.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.moongn.coworkhub.common.utils.JwtUtils;
import cn.moongn.coworkhub.common.exception.ApiException;
import cn.moongn.coworkhub.mapper.RoleMapper;
import cn.moongn.coworkhub.model.Role;
import cn.moongn.coworkhub.model.vo.LoginVO;
import cn.moongn.coworkhub.mapper.UserMapper;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.LoginDTO;
import cn.moongn.coworkhub.model.vo.RegisterVO;
import cn.moongn.coworkhub.service.AuthService;
import cn.moongn.coworkhub.service.PermissionService;
import cn.moongn.coworkhub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionService permissionService;

    @Autowired
    public AuthServiceImpl(PasswordEncoder passwordEncoder,
                           UserService userService,
                           JwtUtils jwtUtils,
                           UserMapper userMapper,
                           RoleMapper roleMapper,
                           PermissionService permissionService) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionService = permissionService;
    }

    @Override
    @Transactional
    public void register(RegisterVO registerVO) {
        // 检查用户名是否已存在
        if (getUser(registerVO.getUsername()) != null) {
            throw new ApiException("用户名已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerVO.getUsername());
        user.setPassword(passwordEncoder.encode(registerVO.getPassword()));

        // 保存用户
        userService.save(user);
    }

    @Override
    public LoginDTO getUser(String username) {
        User user = userMapper.getByUsername(username);

        return BeanUtil.copyProperties(user, LoginDTO.class);
    }

    @Override
    public Map<String, Object> login(LoginVO loginVO) {
        // 获取用户信息
        User user = userMapper.getByUsername(loginVO.getUsername());
        if (user == null) {
            throw new ApiException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginVO.getPassword(), user.getPassword())) {
            throw new ApiException("用户名或密码错误");
        }

        // 用户状态检查
        if (user.getStatus() != 1) {
            throw new ApiException("账号已被禁用，请联系管理员");
        }

        // 生成令牌
        String token = jwtUtils.generateToken(user.getUsername());

        // 构造返回数据
        Map<String, Object> result = new HashMap<>();

        // 创建DTO对象
        LoginDTO loginDTO = new LoginDTO();
        BeanUtil.copyProperties(user, loginDTO);
        loginDTO.setPassword(null); // 清除密码

        // 获取角色信息
        if (user.getRoleId() != null) {
            Role role = roleMapper.getById(user.getRoleId());
            if (role != null) {
                loginDTO.setRoleName(role.getName());
            }
        }

        // 获取权限列表
        List<String> permissions = permissionService.getUserPermissionCodes(user.getId());
        loginDTO.setPermissions(permissions);

        result.put("token", token);
        result.put("user", loginDTO);

        return result;
    }

    public void logout() {
        // 可以在这里添加一些清理工作，比如清除用户的缓存等
        SecurityContextHolder.clearContext();
    }
}