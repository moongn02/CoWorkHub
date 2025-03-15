package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.ChangePasswordRequest;
import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.UserDTO;

public interface UserService {
    UserDTO getById(Long id);
    User getCurrentUser();
    User getByUsername(String username);
    UserDTO save(User user);
    void update(User user);
    void changePassword(ChangePasswordRequest request);
}