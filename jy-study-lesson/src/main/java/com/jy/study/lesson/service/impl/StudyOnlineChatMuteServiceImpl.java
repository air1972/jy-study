package com.jy.study.lesson.service.impl;

import com.jy.study.common.utils.DateUtils;
import com.jy.study.lesson.domain.StudyOnlineChatMute;
import com.jy.study.lesson.mapper.StudyOnlineChatMuteMapper;
import com.jy.study.lesson.service.IStudyOnlineChatMuteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 在线聊天室禁言 Service 实现
 */
@Service
public class StudyOnlineChatMuteServiceImpl implements IStudyOnlineChatMuteService {
    @Autowired
    private StudyOnlineChatMuteMapper studyOnlineChatMuteMapper;

    @Override
    public StudyOnlineChatMute selectActiveMute(Long userId, String roomCode, Date now) {
        return studyOnlineChatMuteMapper.selectActiveMute(userId, roomCode, now);
    }

    @Override
    public int insertStudyOnlineChatMute(StudyOnlineChatMute mute) {
        return studyOnlineChatMuteMapper.insertStudyOnlineChatMute(mute);
    }

    @Override
    public int disableRoomMute(Long userId, String roomCode, String updateBy) {
        return studyOnlineChatMuteMapper.disableRoomMute(userId, roomCode, updateBy, DateUtils.getNowDate());
    }
}
