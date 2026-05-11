package org.example.nodediary.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchDetailVO {
    private Boolean inQueue;
    private Boolean matched;
    private Integer matchedUserId;
    private String matchedNickname;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private List<String> matchedUserTags;
}
