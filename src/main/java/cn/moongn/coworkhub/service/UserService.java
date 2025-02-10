package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.User;

public interface UserService {
    User getByUsername(String username);
    User saveUser(User user);
    void delete(Long id);
    User update(User user);
    User getById(Long id);
} 