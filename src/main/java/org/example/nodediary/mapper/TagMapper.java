package org.example.nodediary.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.nodediary.pojo.Tag;

import java.util.List;

@Mapper
public interface TagMapper {

    List<Tag> selectAllTags();

    List<Integer> selectUserTagIds(Integer userId);

    void deleteUserTags(Integer userId);

    void insertUserTags(@Param("userId") Integer userId, @Param("tagIds") List<Integer> tagIds);

    // 根据标签ID列表查标签名称
    List<String> selectTagNamesByIds(@Param("tagIds") List<Integer> tagIds);
}
