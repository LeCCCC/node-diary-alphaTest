package org.example.nodediary.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.nodediary.pojo.MatchRelation;

import java.util.List;

@Mapper
public interface MatchMapper {
    //根据用户id查询匹配用户
    @Select("select matched_user_id from match_relation where user_id = #{userId}")
    public Integer selectMatchUserId(Integer userId);
    //根据用户id查询是否在匹配队列中
    @Select("select user_id from match_relation where user_id = #{userId}")
    public Integer selectUserId(Integer userId);
    //加入匹配队列
    void insertMatchRelation(Integer currentUserId);
    //更新匹配关系
    int updateMatchedRelation(@Param("userId") Integer userId,
                              @Param("matchedUserId") Integer matchedUserId);
    //查询最早进入队列的两个待匹配用户
    List<MatchRelation> selectTop2WaitingUsers();
    //根据用户id查询匹配详情
    MatchRelation selectByUserId(Integer currentUserId);
    //根据用户id查询匹配对象关系
    MatchRelation selectRelationByUserId(Integer currentUserId);
    int deleteByUserId(Integer currentUserId);
    //根据双方用户id删除一条精确匹配关系
    int deleteMatchedRelation(@Param("userId") Integer userId,
                              @Param("matchedUserId") Integer matchedUserId);
}
