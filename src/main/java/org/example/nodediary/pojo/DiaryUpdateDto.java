package org.example.nodediary.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiaryUpdateDto {
    private String title;
    private String content;
    private String coverImage;
    private Integer visibility;
}
