package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.ChangePasswordRequest;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.UserDTO;

public interface UserService {
    User getById(Long id);
    User getCurrentUser();
    UserDTO formatUser(User user);
    User getByUsername(String username);
    void save(User user);
    void update(User user);
    void changePassword(ChangePasswordRequest request);
}