package org.example.nodediary.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiaryStreamVO {
    private Long id;
    private Integer userId;
    private String authorType;
    private String title;
    private String summary;
    private String coverImage;
    private Integer visibility;
    private LocalDateTime createdAt;
}
