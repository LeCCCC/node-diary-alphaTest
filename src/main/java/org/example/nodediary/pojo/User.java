package org.example.nodediary.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    private Integer id;//用户id
    private String username;//用户名
    private String password;//密码
    private String nickname;//昵称
    private Short gender;//性别 0 女 1 男 2不愿透露
    private String avatarUrl;//头像url
    private LocalDateTime createdAt;//创建时间
}
