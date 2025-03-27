package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.User;
import cn.moongn.coworkhub.model.dto.UserDTO;
import cn.moongn.coworkhub.model.vo.ResetPasswordVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface UserService {
    User getById(Long id);
    User getCurrentUser();
    UserDTO formatUser(User user);
    User getByUsername(String username);
    void save(User user);
    void update(User user);
    void changePassword(@Valid @RequestBody ResetPasswordVO resetPasswordVO);
    List<UserDTO> getUserList();
}