package org.example.nodediary.service;

import org.example.nodediary.pojo.*;

public interface DiaryService {
    Long createDiary(DiaryCreateDto dto);

    void updateDiary(Long id, DiaryUpdateDto dto);

    void deleteDiary(Long id);

    PageResult<DiaryListItemVO> getMyDiaryList(Integer pageNum, Integer pageSize);

    DiaryDetailVO getDiaryDetail(Long id);

    PageResult<DiaryStreamVO> getHomeFeed(Integer pageNum, Integer pageSize);
}
