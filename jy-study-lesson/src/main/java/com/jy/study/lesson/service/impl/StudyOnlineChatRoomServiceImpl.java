package com.jy.study.lesson.service.impl;

import com.jy.study.common.exception.ServiceException;
import com.jy.study.common.utils.DateUtils;
import com.jy.study.common.utils.StringUtils;
import com.jy.study.lesson.domain.StudyOnlineChatRoom;
import com.jy.study.lesson.domain.StudyOnlineChatRoomUser;
import com.jy.study.lesson.mapper.StudyOnlineChatMessageMapper;
import com.jy.study.lesson.mapper.StudyOnlineChatMuteMapper;
import com.jy.study.lesson.mapper.StudyOnlineChatOperationLogMapper;
import com.jy.study.lesson.mapper.StudyOnlineChatRoomMapper;
import com.jy.study.lesson.mapper.StudyOnlineChatRoomUserMapper;
import com.jy.study.lesson.service.IStudyOnlineChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 在线聊天室房间 Service 实现
 */
@Service
public class StudyOnlineChatRoomServiceImpl implements IStudyOnlineChatRoomService {
    private static final String PUBLIC_ROOM_CODE = "public-room";
    private static final String DEFAULT_PUBLIC_ROOM_NAME = "公共学习讨论区";
    private static final String ROLE_OWNER = "owner";
    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_MEMBER = "member";
    private static final String ROLE_VISITOR = "visitor";
    private static final String STATUS_NORMAL = "0";
    private static final String STATUS_CLOSED = "1";

    @Autowired
    private StudyOnlineChatRoomMapper studyOnlineChatRoomMapper;

    @Autowired
    private StudyOnlineChatRoomUserMapper studyOnlineChatRoomUserMapper;

    @Autowired
    private StudyOnlineChatMessageMapper studyOnlineChatMessageMapper;

    @Autowired
    private StudyOnlineChatMuteMapper studyOnlineChatMuteMapper;

    @Autowired
    private StudyOnlineChatOperationLogMapper studyOnlineChatOperationLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyOnlineChatRoom ensureRoomAndJoin(String roomCode, Long userId, String loginName, String userName) {
        validateUser(userId, loginName);

        String safeRoomCode = normalizeRoomCode(roomCode);
        StudyOnlineChatRoom room = studyOnlineChatRoomMapper.selectEnabledRoomByCode(safeRoomCode);
        String displayName = resolveDisplayName(loginName, userName);
        if (room == null) {
            if (PUBLIC_ROOM_CODE.equals(safeRoomCode)) {
                room = createRoomInternal(
                        safeRoomCode,
                        defaultRoomName(safeRoomCode),
                        userId,
                        loginName,
                        displayName,
                        loginName
                );
            } else {
                throw new ServiceException("房间不存在，请先创建房间");
            }
        }

        String role = userId.equals(room.getOwnerUserId()) ? ROLE_OWNER : ROLE_MEMBER;
        upsertRoomUser(safeRoomCode, userId, loginName, displayName, role, loginName);
        room.setMyRole(selectMyRole(safeRoomCode, userId));
        return room;
    }

    @Override
    public List<StudyOnlineChatRoom> selectEnabledRoomsWithMyRole(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<StudyOnlineChatRoom> rooms = studyOnlineChatRoomMapper.selectEnabledRoomsWithMyRole(userId);
        return rooms == null ? Collections.emptyList() : rooms;
    }

    @Override
    public StudyOnlineChatRoom selectEnabledRoomByCode(String roomCode) {
        String safeRoomCode = normalizeRoomCode(roomCode);
        return studyOnlineChatRoomMapper.selectEnabledRoomByCode(safeRoomCode);
    }

    @Override
    public List<StudyOnlineChatRoomUser> selectEnabledRoomUsers(String roomCode) {
        String safeRoomCode = normalizeRoomCode(roomCode);
        List<StudyOnlineChatRoomUser> users = studyOnlineChatRoomUserMapper.selectEnabledRoomUsers(safeRoomCode);
        return users == null ? Collections.emptyList() : users;
    }

    @Override
    public String selectMyRole(String roomCode, Long userId) {
        if (userId == null) {
            return ROLE_VISITOR;
        }
        String safeRoomCode = normalizeRoomCode(roomCode);
        StudyOnlineChatRoom room = studyOnlineChatRoomMapper.selectEnabledRoomByCode(safeRoomCode);
        if (room == null) {
            return ROLE_VISITOR;
        }
        if (userId.equals(room.getOwnerUserId())) {
            return ROLE_OWNER;
        }
        StudyOnlineChatRoomUser roomUser = studyOnlineChatRoomUserMapper.selectRoomUser(safeRoomCode, userId);
        if (roomUser == null || StringUtils.isBlank(roomUser.getRole())) {
            return ROLE_VISITOR;
        }
        return roomUser.getRole();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyOnlineChatRoom createRoom(String roomCode, String roomName, Long userId, String loginName, String userName, String operator) {
        validateUser(userId, loginName);

        String safeRoomCode = normalizeRoomCode(roomCode);
        StudyOnlineChatRoom existedRoom = studyOnlineChatRoomMapper.selectRoomByCode(safeRoomCode);
        if (existedRoom != null) {
            if (STATUS_NORMAL.equals(existedRoom.getStatus())) {
                throw new ServiceException("房间编码已存在，请换一个");
            }
            if (STATUS_CLOSED.equals(existedRoom.getStatus())) {
                purgeRoomData(safeRoomCode);
            }
        }
        return createRoomInternal(
                safeRoomCode,
                normalizeRoomName(roomName, safeRoomCode),
                userId,
                loginName,
                resolveDisplayName(loginName, userName),
                StringUtils.defaultIfBlank(operator, loginName)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setAdmin(String roomCode, Long operatorUserId, Long targetUserId, boolean admin, String updateBy) {
        if (operatorUserId == null || targetUserId == null) {
            throw new ServiceException("参数错误");
        }
        String safeRoomCode = normalizeRoomCode(roomCode);
        StudyOnlineChatRoom room = requireRoom(safeRoomCode);
        ensureOwnerOperator(room, operatorUserId);
        if (targetUserId.equals(room.getOwnerUserId())) {
            throw new ServiceException("群主角色不可修改");
        }

        StudyOnlineChatRoomUser targetUser = studyOnlineChatRoomUserMapper.selectRoomUser(safeRoomCode, targetUserId);
        if (targetUser == null) {
            throw new ServiceException("目标用户不在该房间");
        }

        String nextRole = admin ? ROLE_ADMIN : ROLE_MEMBER;
        studyOnlineChatRoomUserMapper.updateRoomUserRole(safeRoomCode, targetUserId, nextRole, updateBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferOwner(String roomCode, Long operatorUserId, Long targetUserId, String updateBy) {
        if (operatorUserId == null || targetUserId == null) {
            throw new ServiceException("参数错误");
        }
        String safeRoomCode = normalizeRoomCode(roomCode);
        StudyOnlineChatRoom room = requireRoom(safeRoomCode);
        ensureOwnerOperator(room, operatorUserId);
        if (targetUserId.equals(operatorUserId)) {
            throw new ServiceException("你已经是群主");
        }

        StudyOnlineChatRoomUser targetUser = studyOnlineChatRoomUserMapper.selectRoomUser(safeRoomCode, targetUserId);
        if (targetUser == null) {
            throw new ServiceException("目标用户不在该房间");
        }

        Date now = DateUtils.getNowDate();
        StudyOnlineChatRoom updateRoom = new StudyOnlineChatRoom();
        updateRoom.setRoomCode(safeRoomCode);
        updateRoom.setOwnerUserId(targetUser.getUserId());
        updateRoom.setOwnerLoginName(targetUser.getLoginName());
        updateRoom.setOwnerUserName(targetUser.getUserName());
        updateRoom.setUpdateBy(updateBy);
        updateRoom.setUpdateTime(now);
        studyOnlineChatRoomMapper.updateRoomOwner(updateRoom);

        studyOnlineChatRoomUserMapper.updateRoomUserRole(safeRoomCode, targetUser.getUserId(), ROLE_OWNER, updateBy);
        studyOnlineChatRoomUserMapper.updateRoomUserRole(safeRoomCode, operatorUserId, ROLE_MEMBER, updateBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoom(String roomCode, Long operatorUserId, String updateBy) {
        if (operatorUserId == null) {
            throw new ServiceException("参数错误");
        }
        String safeRoomCode = normalizeRoomCode(roomCode);
        if (PUBLIC_ROOM_CODE.equals(safeRoomCode)) {
            throw new ServiceException("公共房间不允许删除");
        }

        StudyOnlineChatRoom room = requireRoom(safeRoomCode);
        ensureOwnerOperator(room, operatorUserId);
        purgeRoomData(safeRoomCode);
    }

    @Override
    public boolean canManageRoom(String roomCode, Long userId) {
        String role = selectMyRole(roomCode, userId);
        return ROLE_OWNER.equals(role) || ROLE_ADMIN.equals(role);
    }

    private StudyOnlineChatRoom createRoomInternal(String roomCode, String roomName, Long ownerUserId,
                                                   String ownerLoginName, String ownerUserName, String operator) {
        Date now = DateUtils.getNowDate();
        StudyOnlineChatRoom room = new StudyOnlineChatRoom();
        room.setRoomCode(roomCode);
        room.setRoomName(normalizeRoomName(roomName, roomCode));
        room.setOwnerUserId(ownerUserId);
        room.setOwnerLoginName(ownerLoginName);
        room.setOwnerUserName(ownerUserName);
        room.setStatus(STATUS_NORMAL);
        room.setCreateBy(operator);
        room.setCreateTime(now);
        room.setUpdateBy(operator);
        room.setUpdateTime(now);
        try {
            studyOnlineChatRoomMapper.insertStudyOnlineChatRoom(room);
        } catch (DuplicateKeyException e) {
            throw new ServiceException("房间编码已存在，请换一个");
        }

        upsertRoomUser(roomCode, ownerUserId, ownerLoginName, ownerUserName, ROLE_OWNER, operator);
        room.setMyRole(ROLE_OWNER);
        return room;
    }

    private void upsertRoomUser(String roomCode, Long userId, String loginName, String userName, String role, String operator) {
        StudyOnlineChatRoomUser roomUser = new StudyOnlineChatRoomUser();
        Date now = DateUtils.getNowDate();
        roomUser.setRoomCode(roomCode);
        roomUser.setUserId(userId);
        roomUser.setLoginName(loginName);
        roomUser.setUserName(userName);
        roomUser.setRole(role);
        roomUser.setStatus(STATUS_NORMAL);
        roomUser.setCreateBy(operator);
        roomUser.setCreateTime(now);
        roomUser.setUpdateBy(operator);
        roomUser.setUpdateTime(now);
        studyOnlineChatRoomUserMapper.upsertRoomUser(roomUser);
    }

    private String resolveDisplayName(String loginName, String userName) {
        return StringUtils.defaultIfBlank(userName, loginName);
    }

    private String normalizeRoomName(String roomName, String roomCode) {
        String normalized = StringUtils.defaultIfBlank(roomName, roomCode).trim();
        if (normalized.length() > 64) {
            normalized = normalized.substring(0, 64);
        }
        return normalized;
    }

    private String defaultRoomName(String roomCode) {
        if (PUBLIC_ROOM_CODE.equals(roomCode)) {
            return DEFAULT_PUBLIC_ROOM_NAME;
        }
        return roomCode;
    }

    private void purgeRoomData(String roomCode) {
        studyOnlineChatMessageMapper.deleteMessagesByRoomCode(roomCode);
        studyOnlineChatMuteMapper.deleteMutesByRoomCode(roomCode);
        studyOnlineChatOperationLogMapper.deleteOperationLogsByRoomCode(roomCode);
        studyOnlineChatRoomUserMapper.deleteRoomUsersByRoomCode(roomCode);
        int affected = studyOnlineChatRoomMapper.deleteRoomByCode(roomCode);
        if (affected <= 0) {
            throw new ServiceException("删除群聊失败，请稍后再试");
        }
    }

    private StudyOnlineChatRoom requireRoom(String roomCode) {
        StudyOnlineChatRoom room = studyOnlineChatRoomMapper.selectEnabledRoomByCode(roomCode);
        if (room == null) {
            throw new ServiceException("房间不存在或已关闭");
        }
        return room;
    }

    private void ensureOwnerOperator(StudyOnlineChatRoom room, Long operatorUserId) {
        if (room == null || operatorUserId == null || !operatorUserId.equals(room.getOwnerUserId())) {
            throw new ServiceException("只有群主可以执行该操作");
        }
    }

    private void validateUser(Long userId, String loginName) {
        if (userId == null || StringUtils.isBlank(loginName)) {
            throw new ServiceException("请先登录");
        }
    }

    private String normalizeRoomCode(String roomCode) {
        String safeCode = StringUtils.defaultIfBlank(roomCode, PUBLIC_ROOM_CODE).trim();
        if (!safeCode.matches("^[a-zA-Z0-9_-]{1,64}$")) {
            throw new ServiceException("房间编码仅支持字母、数字、下划线和中划线，长度1-64");
        }
        return safeCode;
    }
}
