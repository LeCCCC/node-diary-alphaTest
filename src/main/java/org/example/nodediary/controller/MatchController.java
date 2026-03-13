package org.example.nodediary.controller;

import org.example.nodediary.pojo.MatchDetailVO;
import org.example.nodediary.pojo.Result;
import org.example.nodediary.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/match")
public class MatchController {
    @Autowired
    MatchService matchService;

    //加入随机匹配
    @PostMapping("/join")
    public Result joinMatch() {
        matchService.joinMatch();
        return Result.success("加入匹配成功");
    }
    //查询匹配详情
    @GetMapping("/current")
    public Result getMatchDetail() {
        MatchDetailVO matchDetailVO = matchService.getMatchDetail();
        return Result.success("success",matchDetailVO);
    }
    //删除匹配
    @DeleteMapping("/current")
    public Result cancelMatch() {
        matchService.cancelMatch();
        return Result.success("解除匹配成功", null);
    }
}
