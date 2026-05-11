package org.example.nodediary.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Tag {
    private Integer id;
    private String name;
    private String category;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
