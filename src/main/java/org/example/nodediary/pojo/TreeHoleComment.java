package org.example.nodediary.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TreeHoleComment {
    private Long commentId;
    private Long treeHoleId;
    private Long userId;
    private String content;
    private LocalDateTime createTime;
}
