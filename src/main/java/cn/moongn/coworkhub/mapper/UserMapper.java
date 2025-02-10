package cn.moongn.coworkhub.mapper;

import cn.moongn.coworkhub.model.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    User findByUsername(@Param("username") String username);
    int insert(User user);
    int update(User user);
    int deleteById(@Param("id") Long id);
    User findById(@Param("id") Long id);
}