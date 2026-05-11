package org.example.nodediary.service.Imp;

import org.example.nodediary.annotation.ClearDiaryCache;
import org.example.nodediary.exception.BusinessException;
import org.example.nodediary.mapper.MatchMapper;
import org.example.nodediary.mapper.TagMapper;
import org.example.nodediary.mapper.UserMapper;
import org.example.nodediary.pojo.BaseContext;
import org.example.nodediary.pojo.MatchDetailVO;
import org.example.nodediary.pojo.MatchRelation;
import org.example.nodediary.pojo.User;
import org.example.nodediary.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
@Service
public class MatchServiceImp implements MatchService {
    @Autowired
    MatchMapper matchMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    TagMapper tagMapper;

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
        while (true) {
            // 1. 查询所有待匹配用户
            List<MatchRelation> waitingUsers = matchMapper.selectAllWaitingUsers();
            if (waitingUsers == null || waitingUsers.size() < 2) {
                break;
            }

            // 2. 构建每个用户 -> tag 集合的映射
            Map<Integer, Set<Integer>> userTagMap = new HashMap<>();
            for (MatchRelation u : waitingUsers) {
                List<Integer> tagIds = tagMapper.selectUserTagIds(u.getUserId());
                if (tagIds != null && !tagIds.isEmpty()) {
                    userTagMap.put(u.getUserId(), new HashSet<>(tagIds));
                }
            }

            // 3. 找 tag 重合度最高的配对
            Integer bestUserId1 = null;
            Integer bestUserId2 = null;
            int bestOverlap = 0;

            List<MatchRelation> users = waitingUsers;
            for (int i = 0; i < users.size(); i++) {
                Integer id1 = users.get(i).getUserId();
                Set<Integer> tags1 = userTagMap.get(id1);
                for (int j = i + 1; j < users.size(); j++) {
                    Integer id2 = users.get(j).getUserId();
                    Set<Integer> tags2 = userTagMap.get(id2);
                    if (tags1 != null && tags2 != null) {
                        int overlap = 0;
                        for (Integer tag : tags1) {
                            if (tags2.contains(tag)) overlap++;
                        }
                        if (overlap > bestOverlap) {
                            bestOverlap = overlap;
                            bestUserId1 = id1;
                            bestUserId2 = id2;
                        }
                    }
                }
            }

            // 4. 确定配对的两人
            Integer userId1, userId2;
            if (bestOverlap > 0) {
                userId1 = bestUserId1;
                userId2 = bestUserId2;
            } else {
                // 兜底：按时间顺序取最早两个
                userId1 = waitingUsers.get(0).getUserId();
                userId2 = waitingUsers.get(1).getUserId();
            }

            // 5. 更新双方匹配关系（FOR UPDATE 已锁定这些行，理论上一定成功）
            int updated1 = matchMapper.updateMatchedRelation(userId1, userId2);
            int updated2 = matchMapper.updateMatchedRelation(userId2, userId1);
            // 仅回滚实际被修改的一方，不误删另一方可能的新关系
            if (updated1 != 1) {
                matchMapper.deleteByUserId(userId1);
            }
            if (updated2 != 1) {
                matchMapper.deleteByUserId(userId2);
            }
        }
    }

    //加入匹配
    @Override
    @Transactional
    public void joinMatch() {
        Integer currentUserId = BaseContext.getCurrentId();
        // INSERT ... WHERE NOT EXISTS，MySQL 保证原子性
        int inserted = matchMapper.insertMatchRelation(currentUserId);
        if (inserted == 0) {
            throw new BusinessException("您已经在匹配队列中或匹配成功了");
        }
    }

    //查询匹配详情
    @Override
    public MatchDetailVO getMatchDetail() {
        Integer currentUserId = BaseContext.getCurrentId();
        //查询匹配关系
        MatchRelation relation = matchMapper.selectRelationByUserId(currentUserId);
        //判断是否匹配成功
        if (relation == null  ) {
            return new MatchDetailVO(false, null, null, null, null, null, null);
        } else if (relation.getStatus() == 0) {
            return new MatchDetailVO(true, false, null, null, null, null, null);
        }
        //查询匹配用户信息
        Integer matchedUserId = relation.getMatchedUserId();
        User user = userMapper.selectById(matchedUserId);

        MatchDetailVO vo = new MatchDetailVO(
                true,
                true,
                matchedUserId,
                user.getNickname(),
                user.getAvatarUrl(),
                relation.getCreatedAt(),
                null
        );

        // 查询匹配对象的标签名称
        List<Integer> matchedTagIds = tagMapper.selectUserTagIds(matchedUserId);
        if (matchedTagIds != null && !matchedTagIds.isEmpty()) {
            List<String> tagNames = tagMapper.selectTagNamesByIds(matchedTagIds);
            vo.setMatchedUserTags(tagNames);
        }

        return vo;
    }

    //解除匹配关系
    @Override
    @ClearDiaryCache
    @Transactional
    public void cancelMatch() {
        Integer currentUserId = BaseContext.getCurrentId();
        // FOR UPDATE 行锁，防止并发取消或与 doMatch 交叉
        MatchRelation relation = matchMapper.selectRelationByUserIdForUpdate(currentUserId);

        if (relation == null || relation.getStatus() == 0) {
            throw new BusinessException("当前没有匹配关系");
        }

        Integer matchedUserId = relation.getMatchedUserId();

        // 删除双方匹配记录（WHERE status=1 + matched_user_id 精确匹配，防误删）
        matchMapper.deleteMatchedRelation(currentUserId, matchedUserId);
        matchMapper.deleteMatchedRelation(matchedUserId, currentUserId);
    }

    //退出队列
    @Override
    @Transactional
    public void quitQueue() {
        Integer currentUserId = BaseContext.getCurrentId();
        // FOR UPDATE 行锁，防止与 doMatch 交叉
        MatchRelation relation = matchMapper.selectRelationByUserIdForUpdate(currentUserId);

        if (relation == null) {
            throw new BusinessException("当前没有加入匹配");
        }
        if (relation.getStatus() == 1) {
            throw new BusinessException("当前已匹配，请先解除匹配关系");
        }

        // 删除匹配队列记录
        matchMapper.deleteByUserId(currentUserId);
    }
}
