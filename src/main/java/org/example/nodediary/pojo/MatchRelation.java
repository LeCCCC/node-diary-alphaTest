package org.example.nodediary.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MatchRelation {
    private Integer id;//匹配关系id
    private Integer userId;//用户id
     private Integer matchedUserId;//匹配用户id
     private LocalDateTime createdAt;//创建时间
     private Integer status;//匹配状态: 0-待处理, 1-已匹配
}
