package org.example.nodediary.service;

import org.example.nodediary.pojo.PageResult;
import org.example.nodediary.pojo.TreeHoleCommentVO;

public interface TreeHoleCommentService {
    // 分页查询评论列表
    PageResult<TreeHoleCommentVO> getCommentList(Long treeHoleId, Integer pageNum, Integer pageSize);

    // 发表评论
    Long publishComment(Long treeHoleId, String content);
}
