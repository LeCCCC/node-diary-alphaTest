package org.example.nodediary.service.Imp;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.nodediary.annotation.ClearDiaryCache;
import org.example.nodediary.exception.BusinessException;
import org.example.nodediary.mapper.DiaryMapper;
import org.example.nodediary.mapper.MatchMapper;
import org.example.nodediary.mapper.UserMapper;
import org.example.nodediary.pojo.*;
import org.example.nodediary.service.DiaryService;
import org.example.nodediary.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DiaryServiceImp implements DiaryService {
    @Autowired
    private DiaryMapper diaryMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MatchService matchService;
    @Autowired
    private MatchMapper matchMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    //创建新日记
    @ClearDiaryCache
    @Override
    public Long createDiary(DiaryCreateDto dto) {
        //异常处理
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new BusinessException("标题不能为空");
        } else if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new BusinessException("内容不能为空");
        } else if (dto.getVisibility() == null || (dto.getVisibility() != 0 && dto.getVisibility() != 1)) {
            throw new BusinessException("可见范围参数非法");
        }
        Diary diary = new Diary();
        // 从当前登录上下文中取用户 id
        diary.setUserId(BaseContext.getCurrentId());
        diary.setTitle(dto.getTitle());
        diary.setContent(dto.getContent());
        diary.setCoverImage(dto.getCoverImage());
        diary.setVisibility(dto.getVisibility());
        diary.setCreatedAt(LocalDateTime.now());
        diary.setUpdatedAt(LocalDateTime.now());

        // 保存到数据库
        diaryMapper.insert(diary);
        return diary.getId();
    }

    //更新日记
    @ClearDiaryCache
    @Override
    public void updateDiary(Long id, DiaryUpdateDto dto) {
        Diary diary = diaryMapper.selectById(id);
        //异常处理
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new BusinessException("标题不能为空");
        } else if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new BusinessException("内容不能为空");
        } else if (diary == null) {
            throw new BusinessException("日记不存在");
        } else if (!diary.getUserId().equals(BaseContext.getCurrentId())) {
            throw new BusinessException("您没有权限更新此日记");
        }

        // 更新日记字段
        diary.setTitle(dto.getTitle());
        diary.setContent(dto.getContent());
        diary.setCoverImage(dto.getCoverImage());
        diary.setVisibility(dto.getVisibility());
        diary.setUpdatedAt(LocalDateTime.now());

        // 保存到数据库
        diaryMapper.updateById(diary);
    }

    //删除日记
    @ClearDiaryCache
    @Override
    public void deleteDiary(Long id) {
        Diary diary = diaryMapper.selectById(id);
        //异常处理
        if (diary == null) {
            throw new BusinessException("日记不存在");
        } else if (!diary.getUserId().equals(BaseContext.getCurrentId())) {
            throw new BusinessException("您没有权限删除此日记");
        }

        // 从数据库删除
        diaryMapper.deleteById(id);
    }

    //获取用户日记列表
    @Override
    public PageResult<DiaryListItemVO> getMyDiaryList(Integer pageNum, Integer pageSize) {
        //获取用户ID
        Integer userId = BaseContext.getCurrentId();
        //设置查询起始位置，公式为（当前页码 - 1）* 每页条数
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        int offset = (pageNum - 1) * pageSize;

        //查Redis
        String key = "cache:diary_list:" + userId;
        String cacheResult = stringRedisTemplate.opsForValue().get(key);
        //判断缓存是否为空
        if(StrUtil.isNotBlank(cacheResult)){
            PageResult pageResult = JSONUtil.toBean(cacheResult, PageResult.class);
            log.info("此次日记列表查的缓存");
            return pageResult;
        }


        //计算该用户总记录数
        Long total = diaryMapper.countByUserId(userId);
        //查询该用户分页日记列表
        List<Diary> diaryList = diaryMapper.selectPageByUserId(userId, offset, pageSize);
        //创建返回对象列表
        List<DiaryListItemVO> records = new ArrayList<>();
        //对Mapper传回的列表进行遍历，为返回对象赋值
        for (Diary diary : diaryList) {
            DiaryListItemVO vo = new DiaryListItemVO();
            vo.setId(diary.getId());
            vo.setTitle(diary.getTitle());
            vo.setCoverImage(diary.getCoverImage());
            vo.setVisibility(diary.getVisibility());
            vo.setCreatedAt(diary.getCreatedAt());
            // 生成摘要
            vo.setSummary(buildSummary(diary.getContent()));
            records.add(vo);
        }

        //设置随机失效时间防止缓存雪崩
        Random random = new Random();
        int expireTime = random.nextInt(5) + 1;
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(new PageResult<>(total, records)), expireTime ,TimeUnit.MINUTES);
        log.info("此次日记列表查的数据库");
        return new PageResult<>(total, records);
    }

    //获取日记详情
    @Override
    public DiaryDetailVO getDiaryDetail(Long id) {
        //获取日记
        Diary diary = diaryMapper.selectById(id);
        //异常处理
        if (diary == null) {
            throw new BusinessException("日记不存在");
        }
        //获取当前发起请求的用户ID
        Integer currentUserId = BaseContext.getCurrentId();
        //判断是否是作者或匹配用户
        boolean isAuthor = diary.getUserId().equals(currentUserId);
        boolean isMatchedUser = matchService.isMatched(currentUserId, diary.getUserId());
        //如果不是作者或者匹配用户，且可见性为0则不可见日记
        if (!isAuthor && !(diary.getVisibility() == 1 && isMatchedUser)) {
            throw new BusinessException("您没有权限查看此日记");
        }

        String nickName = userMapper.selectById(diary.getUserId()).getNickname();

        DiaryDetailVO vo = new DiaryDetailVO();
        vo.setId(diary.getId());
        vo.setUserId(diary.getUserId());
        vo.setAuthorName(nickName);
        vo.setTitle(diary.getTitle());
        vo.setContent(diary.getContent());
        vo.setCoverImage(diary.getCoverImage());
        vo.setVisibility(diary.getVisibility());
        vo.setCreateAt(diary.getCreatedAt());
        vo.setUpdateAt(diary.getUpdatedAt());

        return vo;
    }

    //生成日记摘要
    private String buildSummary(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        // 去掉 HTML 标签
        String plainText = content.replaceAll("<[^>]*>", "").trim();
        if (plainText.length() <= 50) {
            return plainText;
        }
        return plainText.substring(0, 50) + "……";
    }

    //获取首页日记流
    @Override
    public PageResult<DiaryStreamVO> getHomeFeed(Integer pageNum, Integer pageSize) {

        //获取上下文用户Id
        Integer currentUserId = BaseContext.getCurrentId();
        //设置查询起始位置，公式为（当前页码 - 1）* 每页条数
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        int offset = (pageNum - 1) * pageSize;

        //查Redis
        String key = "cache:home_feed:" + currentUserId;
        String cacheResult = stringRedisTemplate.opsForValue().get(key);
        //判断缓存是否为空
        if(StrUtil.isNotBlank(cacheResult)){
            PageResult pageResult = JSONUtil.toBean(cacheResult, PageResult.class);
            log.info("此次首页日记流查的缓存");
            return pageResult;
        }


        // 查匹配对象ID
        Integer matchedUserId = matchMapper.selectMatchUserId(currentUserId);

        // 查总数
        Long total = diaryMapper.countHomeFeed(currentUserId, matchedUserId);

        // 查分页列表
        List<Diary> diaryList = diaryMapper.selectHomeFeed(currentUserId, matchedUserId, offset, pageSize);
        //查询结果填入records
        List<DiaryStreamVO> records = new ArrayList<>();

        for (Diary diary : diaryList) {
            DiaryStreamVO vo = new DiaryStreamVO();
            vo.setId(diary.getId());
            vo.setUserId(diary.getUserId());
            vo.setTitle(diary.getTitle());
            vo.setVisibility(diary.getVisibility());
            vo.setCoverImage(diary.getCoverImage());
            vo.setCreatedAt(diary.getCreatedAt());

            // 摘要
            vo.setSummary(buildSummary(diary.getContent()));

            // 作者类型
            if (diary.getUserId().equals(currentUserId)) {
                vo.setAuthorType("SELF");
            } else {
                vo.setAuthorType("MATCHED");
            }

            records.add(vo);
        }
        log.info("此次首页日记流查的数据库");
        //存入Redis
        //设置随机失效时间防止缓存雪崩
        Random random = new Random();
        int expireTime = random.nextInt(5) + 1;
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(new PageResult<>(total, records)), expireTime ,TimeUnit.MINUTES);

        return new PageResult<>(total, records);
    }
}
