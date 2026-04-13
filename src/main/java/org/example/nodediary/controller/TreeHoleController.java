package org.example.nodediary.controller;

import org.example.nodediary.pojo.*;
import org.example.nodediary.service.TreeHoleCommentService;
import org.example.nodediary.service.TreeHoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tree-holes")
public class TreeHoleController {

    @Autowired
    private TreeHoleService treeHoleService;
    @Autowired
    private TreeHoleCommentService treeHoleCommentService;

    /**
     * 获取树洞列表
     */
    @GetMapping
    public Result getTreeHoleList(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<TreeHoleVO> result = treeHoleService.getTreeHoleList(pageNum, pageSize);
        return Result.success("success", result);
    }

    /**
     * 发布树洞
     */
    @PostMapping
    public Result publishTreeHole(@RequestBody TreeHoleSaveReq req) {
        Long treeHoleId = treeHoleService.publishTreeHole(req.getContent());
        return Result.success("发布成功", new TreeHoleIdVO(treeHoleId));
    }

    /**
     * 获取树洞详情
     */
    @GetMapping("/{treeHoleId}")
    public Result getTreeHoleDetail(@PathVariable Long treeHoleId) {
        TreeHoleVO treeHole = treeHoleService.getTreeHoleDetail(treeHoleId);
        return Result.success("success", treeHole);
    }

    /**
     * 修改树洞
     */
    @PutMapping("/{treeHoleId}")
    public Result updateTreeHole(@PathVariable Long treeHoleId,
                                 @RequestBody TreeHoleSaveReq req) {
        treeHoleService.updateTreeHole(treeHoleId, req.getContent());
        return Result.success("修改成功", null);
    }

    /**
     * 删除树洞
     */
    @DeleteMapping("/{treeHoleId}")
    public Result deleteTreeHole(@PathVariable Long treeHoleId) {
        treeHoleService.deleteTreeHole(treeHoleId);
        return Result.success("删除成功", null);
    }

    /**
     * 获取树洞评论列表
     */
    @GetMapping("/{treeHoleId}/comments")
    public Result getCommentList(@PathVariable Long treeHoleId,
                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "100") Integer pageSize) {
        PageResult<TreeHoleCommentVO> result = treeHoleCommentService.getCommentList(treeHoleId, pageNum, pageSize);
        return Result.success("success", result);
    }

    /**
     * 发表评论
     */
    @PostMapping("/{treeHoleId}/comments")
    public Result publishComment(@PathVariable Long treeHoleId,
                                 @RequestBody TreeHoleCommentCreateReq req) {
        Long commentId = treeHoleCommentService.publishComment(treeHoleId, req.getContent());
        return Result.success("评论成功", new TreeHoleCommentIdVO(commentId));
    }
}
