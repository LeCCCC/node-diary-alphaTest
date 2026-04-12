package org.example.nodediary.service.Imp;

import org.example.nodediary.annotation.ClearDiaryCache;
import org.example.nodediary.exception.BusinessException;
import org.example.nodediary.mapper.MatchMapper;
import org.example.nodediary.mapper.UserMapper;
import org.example.nodediary.pojo.BaseContext;
import org.example.nodediary.pojo.MatchDetailVO;
import org.example.nodediary.pojo.MatchRelation;
import org.example.nodediary.pojo.User;
import org.example.nodediary.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MatchServiceImp implements MatchService {
    @Autowired
    MatchMapper matchMapper;
    @Autowired
    UserMapper userMapper;

    //根据用户id查询是否已经匹配过
    @Override
    public boolean isMatched(Integer currentUserId, Integer userId) {
        Integer matchUserId = matchMapper.selectMatchUserId(userId);
        return matchUserId != null && matchUserId.equals(currentUserId);
    }

    //匹配
    @Transactional
    @ClearDiaryCache
    @Override
    public void doMatch() {
        //while循环可以一直匹配直到用户数量不足以匹配
        while (true) {
            // 1. 查询最早进入队列的两个待匹配用户
            List<MatchRelation> waitingUsers = matchMapper.selectTop2WaitingUsers();

            // 2. 如果不足两人，直接结束
            if (waitingUsers == null || waitingUsers.size() < 2) {
                break;
            }

            MatchRelation user1 = waitingUsers.get(0);
            MatchRelation user2 = waitingUsers.get(1);

            Integer userId1 = user1.getUserId();
            Integer userId2 = user2.getUserId();

            // 3. 更新双方匹配关系
            matchMapper.updateMatchedRelation(userId1, userId2);
            matchMapper.updateMatchedRelation(userId2, userId1);
        }
    }

    //加入匹配
    @Override
    public void joinMatch() {
        Integer currentUserId = BaseContext.getCurrentId();
        //判断是否已经匹配过
        if (matchMapper.selectUserId(currentUserId) != null) {
            throw new BusinessException("您已经在匹配队列中或匹配成功了");
        }
        //加入匹配队列
        matchMapper.insertMatchRelation(currentUserId);
    }

    //查询匹配详情
    @Override
    public MatchDetailVO getMatchDetail() {
        Integer currentUserId = BaseContext.getCurrentId();
        //查询匹配关系
        MatchRelation relation = matchMapper.selectRelationByUserId(currentUserId);
        //判断是否匹配成功
        if (relation == null  ) {
            return new MatchDetailVO(false, null, null, null, null,null);
        }else if(relation.getStatus() == 0){
            return new MatchDetailVO(true, false, null, null, null,null);
        }
        //查询匹配用户信息
        Integer matchedUserId = relation.getMatchedUserId();
        User user = userMapper.selectById(matchedUserId);

        return new MatchDetailVO(
                true,
                true,
                matchedUserId,
                user.getNickname(),
                user.getAvatarUrl(),
                relation.getCreatedAt()
        );
    }

    //解除匹配关系
    @Override
    @ClearDiaryCache
    @Transactional
    public void cancelMatch() {
        Integer currentUserId = BaseContext.getCurrentId();
        // 查询当前匹配关系
        MatchRelation relation = matchMapper.selectRelationByUserId(currentUserId);

        if (relation == null || relation.getStatus() == 0) {
            throw new BusinessException("当前没有匹配关系");
        }

        Integer matchedUserId = relation.getMatchedUserId();

        // 删除双方匹配记录
        matchMapper.deleteByUserId(currentUserId);
        matchMapper.deleteByUserId(matchedUserId);
    }

    //退出队列
    @Override
    public void quitQueue() {
        Integer currentUserId = BaseContext.getCurrentId();
        // 查询当前匹配关系
        MatchRelation relation = matchMapper.selectRelationByUserId(currentUserId);

        if (relation == null ) {
            throw new BusinessException("当前没有加入匹配");
        }

        // 删除匹配队列记录
        matchMapper.deleteByUserId(currentUserId);
    }
}
