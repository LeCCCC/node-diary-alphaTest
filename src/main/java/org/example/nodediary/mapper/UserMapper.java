package org.example.nodediary.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.nodediary.pojo.User;

@Mapper
public interface UserMapper {
    //根据用户名查询用户
    @Select("select * from user where username = #{username}")
    public User selectByUsername(String username);

    //插入用户
    void insertUser(User user);

    //根据Id查询
    @Select("select id,username,nickname,avatar_url,created_at from user where id = #{id}")
    public User selectById(Integer id);

    //根据Id查询（含密码，用于修改密码校验）
    @Select("select * from user where id = #{id}")
    User selectByIdWithPassword(Integer id);

    //更新密码
    @Update("update user set password = #{password} where id = #{id}")
    void updatePassword(@Param("id") Integer id, @Param("password") String password);

    void updateUser(User user);
}
