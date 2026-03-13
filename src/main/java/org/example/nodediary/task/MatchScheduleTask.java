package org.example.nodediary.task;

import org.example.nodediary.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MatchScheduleTask {

    @Autowired
    private MatchService matchService;
    //每隔一分钟执行一次匹配
    @Scheduled(fixedDelay = 60000)
    public void executeMatchTask() {
        try {
            matchService.doMatch();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}