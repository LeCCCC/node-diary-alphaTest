package org.example.nodediary.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.beans.Visibility;
import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Diary {
     private Long id;//日志id
     private Integer userId;//用户id
     private String title;//标题
     private String content;//内容
     private String coverImage;//封面图片url
     private Integer visibility;//可见性 0 仅自己可见 1 彼此可见
     private LocalDateTime updatedAt;//更新时间
     private LocalDateTime createdAt;//创建时间
}
