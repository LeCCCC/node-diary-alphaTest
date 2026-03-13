package org.example.nodediary.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data

public class UserMe {
    private Integer id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
