package org.example.nodediary.service;

import org.example.nodediary.pojo.PageResult;
import org.example.nodediary.pojo.TreeHoleVO;

public interface TreeHoleService {
    // 分页查询树洞列表
    PageResult<TreeHoleVO> getTreeHoleList(Integer pageNum, Integer pageSize);

    // 发布树洞
    Long publishTreeHole(String content);

    // 获取树洞详情
    TreeHoleVO getTreeHoleDetail(Long treeHoleId);

    // 修改树洞
    void updateTreeHole(Long treeHoleId, String content);

    // 删除树洞
    void deleteTreeHole(Long treeHoleId);
}
