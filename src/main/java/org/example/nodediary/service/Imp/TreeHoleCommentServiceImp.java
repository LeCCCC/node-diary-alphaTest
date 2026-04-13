package org.example.nodediary.service.Imp;

import org.example.nodediary.exception.BusinessException;
import org.example.nodediary.mapper.TreeHoleCommentMapper;
import org.example.nodediary.mapper.TreeHoleMapper;
import org.example.nodediary.pojo.BaseContext;
import org.example.nodediary.pojo.PageResult;
import org.example.nodediary.pojo.TreeHoleComment;
import org.example.nodediary.pojo.TreeHoleCommentVO;
import org.example.nodediary.service.TreeHoleCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TreeHoleCommentServiceImp implements TreeHoleCommentService {

    @Autowired
    private TreeHoleCommentMapper treeHoleCommentMapper;
    @Autowired
    private TreeHoleMapper treeHoleMapper;

    @Override
    public PageResult<TreeHoleCommentVO> getCommentList(Long treeHoleId, Integer pageNum, Integer pageSize) {
        if (treeHoleMapper.selectById(treeHoleId) == null) {
            throw new BusinessException("树洞不存在", 404);
        }
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 100;
        long offset = (long) (pageNum - 1) * pageSize;
        List<TreeHoleCommentVO> records = treeHoleCommentMapper.selectPage(treeHoleId, offset, pageSize);
        Long total = treeHoleCommentMapper.countByTreeHoleId(treeHoleId);
        return new PageResult<>(total, records);
    }

    @Override
    public Long publishComment(Long treeHoleId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }
        if (content.length() > 1000) {
            throw new BusinessException("评论内容不能超过1000字");
        }
        if (treeHoleMapper.selectById(treeHoleId) == null) {
            throw new BusinessException("树洞不存在", 404);
        }
        Long userId = Long.valueOf(BaseContext.getCurrentId());
        TreeHoleComment comment = new TreeHoleComment(null, treeHoleId, userId, content.trim(), null);
        treeHoleCommentMapper.insertComment(comment);
        return comment.getCommentId();
    }
}
