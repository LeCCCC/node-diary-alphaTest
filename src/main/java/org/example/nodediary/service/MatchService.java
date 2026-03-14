package org.example.nodediary.service;

import org.example.nodediary.pojo.MatchDetailVO;

public interface MatchService {

    boolean isMatched(Integer currentUserId, Integer userId) ;

    void doMatch();

    void joinMatch();

    MatchDetailVO getMatchDetail();
    //取消匹配关系
    void cancelMatch();
    //退出匹配队列
    void quitQueue();
}
