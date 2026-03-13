package org.example.nodediary.controller;

import org.example.nodediary.pojo.*;
import org.example.nodediary.service.DiaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/diary")
public class DiaryController {
    @Autowired
    DiaryService diaryService;

    //创建新日记
    @PostMapping
    public Result creatDiary(@RequestBody DiaryCreateDto dto) {

        Long diaryId = diaryService.createDiary(dto);
        //结果封装到map以和接口文档对应
        HashMap<String, Long> data = new HashMap<>();
        data.put("id", diaryId);
        return Result.success("创建成功", data);
    }

    //编辑日记
    @PutMapping("/{id}")
    public Result updateDiary(@PathVariable Long id, @RequestBody DiaryUpdateDto dto) {
        diaryService.updateDiary(id, dto);
        return Result.success("更新成功");
    }

    //删除日记
    @DeleteMapping("/{id}")
    public Result deleteDiary(@PathVariable Long id) {
        diaryService.deleteDiary(id);
        return Result.success("删除成功");
    }

    //获取日记列表
    @GetMapping("/my-list")
    public Result getMyDiaryList(
            //设置默认路径参数
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<DiaryListItemVO> pageResult = diaryService.getMyDiaryList(pageNum, pageSize);
        return Result.success("success", pageResult);
    }

    //获取日记详情
    @GetMapping("/{id}")
    public Result getDiaryDetail(@PathVariable Long id) {
        DiaryDetailVO diaryDetailVO = diaryService.getDiaryDetail(id);
        return Result.success("success", diaryDetailVO);
    }

    //获取日记流
    @GetMapping("/home-feed")
    public Result getHomeFeed(
            //设置默认路径参数
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<DiaryStreamVO> pageResult = diaryService.getHomeFeed(pageNum, pageSize);
        return Result.success("success", pageResult);
    }
}