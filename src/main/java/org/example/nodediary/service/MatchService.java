package org.example.nodediary.service;

import org.example.nodediary.pojo.MatchDetailVO;

public interface MatchService {

    boolean isMatched(Integer currentUserId, Integer userId) ;

    void doMatch();

    void joinMatch();

    MatchDetailVO getMatchDetail();

    void cancelMatch();
}
