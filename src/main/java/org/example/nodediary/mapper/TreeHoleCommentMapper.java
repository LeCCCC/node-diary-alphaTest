package org.example.nodediary.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.nodediary.pojo.TreeHoleComment;
import org.example.nodediary.pojo.TreeHoleCommentVO;

import java.util.List;

@Mapper
public interface TreeHoleCommentMapper {
    // 分页查询评论列表
    List<TreeHoleCommentVO> selectPage(@Param("treeHoleId") Long treeHoleId, @Param("offset") Long offset, @Param("pageSize") Integer pageSize);

    // 查询评论总数
    Long countByTreeHoleId(@Param("treeHoleId") Long treeHoleId);

    // 发表评论（回填 commentId）
    void insertComment(TreeHoleComment comment);
}
