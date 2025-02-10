package cn.moongn.coworkhub.service.impl;

import cn.moongn.coworkhub.mapper.UserMapper;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Override
    public User getByUsername(String username) {
        return baseMapper.findByUsername(username);
    }
    
    @Override
    public User saveUser(User user) {
        if (user.getId() == null) {
            baseMapper.insert(user);
        } else {
            baseMapper.update(user);
        }
        return user;
    }
    
    @Override
    public void delete(Long id) {
        baseMapper.deleteById(id);
    }
    
    @Override
    public User update(User user) {
        baseMapper.update(user);
        return user;
    }
    
    @Override
    public User getById(Long id) {
        return baseMapper.findById(id);
    }
}