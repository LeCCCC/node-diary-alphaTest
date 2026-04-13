package org.example.nodediary.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.nodediary.pojo.TreeHole;
import org.example.nodediary.pojo.TreeHoleVO;

import java.util.List;

@Mapper
public interface TreeHoleMapper {
    // 分页查询树洞列表
    List<TreeHoleVO> selectPage(@Param("offset") Long offset, @Param("pageSize") Integer pageSize);

    // 查询树洞总数
    Long countAll();

    // 发布树洞（回填 treeHoleId）
    void insertTreeHole(TreeHole treeHole);

    // 获取树洞详情
    TreeHoleVO selectById(@Param("treeHoleId") Long treeHoleId);

    // 修改树洞
    void updateTreeHole(@Param("treeHoleId") Long treeHoleId, @Param("content") String content);

    // 删除树洞
    void deleteById(@Param("treeHoleId") Long treeHoleId);
}
