package org.example.nodediary.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaryDetailVO {
    private Long id;
    private Integer userId;
    private String authorName;
    private String title;
    private String content;
    private String coverImage;
    private Integer visibility;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
