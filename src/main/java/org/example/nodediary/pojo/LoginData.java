package org.example.nodediary.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LoginData {
    private String token;
    private String tokenType;
    private UserInfo userInfo;
}
