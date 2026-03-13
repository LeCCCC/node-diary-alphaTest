package org.example.nodediary.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaryListItemVO {
    private Long id;
    private String title;
    private String summary;
    private String coverImage;
    private Integer visibility;
    private LocalDateTime createdAt;
}
