package org.example.nodediary.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.nodediary.pojo.Diary;

import java.util.List;

@Mapper
public interface DiaryMapper {
    // 插入新日记
    int insert(Diary diary);
    @Select("select * from diary where id = #{id}")
    Diary selectById(Long id);
    //更新日记
    void updateById(Diary diary);
    // 删除日记
    @Delete("delete from diary where id = #{id}")
    void deleteById(Long id);
    //根据用户ID查询该用户总记录数
    @Select("select count(*) from diary where user_id = #{userId}")
    Long countByUserId(Integer userId);
    //根据用户ID分页查询该用户所有记录
    List<Diary> selectPageByUserId(Integer userId, int offset, Integer pageSize);
    //根据用户ID查询首页分流记录数
    Long countHomeFeed(Integer currentUserId, Integer matchedUserId);
    //查询首页分流列表
    List<Diary> selectHomeFeed(Integer currentUserId, Integer matchedUserId, int offset, Integer pageSize);
}
