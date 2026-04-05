package com.jy.study.web.controller.web;

import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.core.domain.entity.SysUser;
import com.jy.study.common.exception.ServiceException;
import com.jy.study.common.utils.DateUtils;
import com.jy.study.common.utils.StringUtils;
import com.jy.study.lesson.domain.StudyOnlineChatMessage;
import com.jy.study.lesson.domain.StudyOnlineChatMute;
import com.jy.study.lesson.domain.StudyOnlineChatOperationLog;
import com.jy.study.lesson.domain.StudyOnlineChatRoom;
import com.jy.study.lesson.domain.StudyOnlineChatRoomUser;
import com.jy.study.lesson.service.IStudyOnlineChatMessageService;
import com.jy.study.lesson.service.IStudyOnlineChatMuteService;
import com.jy.study.lesson.service.IStudyOnlineChatOperationLogService;
import com.jy.study.lesson.service.IStudyOnlineChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.Collections;
import java.util.List;

/**
 * 前台在线聊天室控制器
 */
@Controller
@RequestMapping("/web/chat")
public class WebChatController extends BaseController {
    private static final String PUBLIC_ROOM_CODE = "public-room";
    private static final int HISTORY_LIMIT = 50;
    private static final String ROLE_OWNER = "owner";
    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_MEMBER = "member";
    private static final String ROLE_VISITOR = "visitor";

    @Autowired
    private IStudyOnlineChatMessageService onlineChatMessageService;

    @Autowired
    private IStudyOnlineChatRoomService onlineChatRoomService;

    @Autowired
    private IStudyOnlineChatMuteService onlineChatMuteService;

    @Autowired
    private IStudyOnlineChatOperationLogService onlineChatOperationLogService;

    @GetMapping({"", "/room"})
    public String room(@RequestParam(value = "roomCode", required = false) String roomCode, ModelMap mmap) {
        SysUser user = getSysUser();
        SysUser safeUser = user == null ? new SysUser(0L) : user;
        if (StringUtils.isBlank(safeUser.getUserName())) {
            safeUser.setUserName(StringUtils.defaultIfBlank(safeUser.getLoginName(), "当前用户"));
        }

        String safeRoomCode = normalizeRoomCode(roomCode);
        mmap.put("user", safeUser);
        mmap.put("roomCode", safeRoomCode);
        mmap.put("rooms", Collections.emptyList());
        mmap.put("roomUsers", Collections.emptyList());
        mmap.put("operationLogs", Collections.emptyList());
        mmap.put("myRole", ROLE_VISITOR);
        mmap.put("canManageRoom", false);
        mmap.put("isOwner", false);
        try {
            if (safeUser.getUserId() == null || safeUser.getUserId() <= 0) {
                mmap.put("chatInitError", "请先登录后使用聊天室功能");
                mmap.put("history", Collections.emptyList());
                return "web/chat-room";
            }

            StudyOnlineChatRoom roomInfo = onlineChatRoomService.ensureRoomAndJoin(
                    safeRoomCode,
                    safeUser.getUserId(),
                    safeUser.getLoginName(),
                    safeUser.getUserName()
            );

            List<StudyOnlineChatRoom> rooms = onlineChatRoomService.selectEnabledRoomsWithMyRole(safeUser.getUserId());
            List<StudyOnlineChatRoomUser> roomUsers = onlineChatRoomService.selectEnabledRoomUsers(safeRoomCode);
            List<StudyOnlineChatOperationLog> operationLogs = onlineChatOperationLogService.selectRecentLogs(safeRoomCode, 20);
            String myRole = onlineChatRoomService.selectMyRole(safeRoomCode, safeUser.getUserId());
            List<StudyOnlineChatMessage> history = onlineChatMessageService.selectRecentMessages(safeRoomCode, HISTORY_LIMIT);

            mmap.put("currentRoom", roomInfo);
            mmap.put("rooms", rooms);
            mmap.put("roomUsers", roomUsers);
            mmap.put("operationLogs", operationLogs);
            mmap.put("myRole", myRole);
            mmap.put("canManageRoom", isManagerRole(myRole));
            mmap.put("isOwner", ROLE_OWNER.equals(myRole));
            mmap.put("history", history);
        } catch (Exception e) {
            logger.error("Load chat history failed.", e);
            mmap.put("history", Collections.emptyList());
            mmap.put("chatInitError", buildFriendlyMessage(e));
        }
        return "web/chat-room";
    }

    @PostMapping("/room/create")
    @ResponseBody
    public AjaxResult createRoom(String roomCode, String roomName) {
        SysUser user = getSysUser();
        if (user == null || user.getUserId() == null) {
            return AjaxResult.error("请先登录");
        }

        try {
            String safeRoomCode = normalizeRoomCodeStrict(roomCode);
            String safeRoomName = normalizeRoomName(roomName, safeRoomCode);
            StudyOnlineChatRoom room = onlineChatRoomService.createRoom(
                    safeRoomCode,
                    safeRoomName,
                    user.getUserId(),
                    user.getLoginName(),
                    user.getUserName(),
                    user.getLoginName()
            );
            recordOperation(safeRoomCode, "create_room", user, null, null,
                    "创建房间：" + safeRoomName + "（" + safeRoomCode + "）");
            return AjaxResult.success("房间创建成功")
                    .put("data", room)
                    .put("roomCode", room.getRoomCode());
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Create chat room failed, roomCode={}", roomCode, e);
            return AjaxResult.error(buildFriendlyMessage(e));
        }
    }

    @PostMapping("/room/admin/set")
    @ResponseBody
    public AjaxResult setAdmin(String roomCode, Long targetUserId, Boolean admin) {
        SysUser user = getSysUser();
        if (user == null || user.getUserId() == null) {
            return AjaxResult.error("请先登录");
        }
        if (targetUserId == null) {
            return AjaxResult.error("请先选择目标成员");
        }

        try {
            String safeRoomCode = normalizeRoomCodeStrict(roomCode);
            if (!ROLE_OWNER.equals(onlineChatRoomService.selectMyRole(safeRoomCode, user.getUserId()))) {
                return AjaxResult.error("只有群主可以设置管理员");
            }
            onlineChatRoomService.setAdmin(
                    safeRoomCode,
                    user.getUserId(),
                    targetUserId,
                    Boolean.TRUE.equals(admin),
                    user.getLoginName()
            );
            String targetName = resolveTargetUserName(safeRoomCode, targetUserId);
            recordOperation(safeRoomCode,
                    Boolean.TRUE.equals(admin) ? "set_admin" : "unset_admin",
                    user, targetUserId, targetName,
                    Boolean.TRUE.equals(admin) ? "设置管理员" : "取消管理员");
            return AjaxResult.success(Boolean.TRUE.equals(admin) ? "已设为管理员" : "已取消管理员");
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Set admin failed, roomCode={}, targetUserId={}", roomCode, targetUserId, e);
            return AjaxResult.error(buildFriendlyMessage(e));
        }
    }

    @PostMapping("/room/owner/transfer")
    @ResponseBody
    public AjaxResult transferOwner(String roomCode, Long targetUserId) {
        SysUser user = getSysUser();
        if (user == null || user.getUserId() == null) {
            return AjaxResult.error("请先登录");
        }
        if (targetUserId == null) {
            return AjaxResult.error("请先选择目标成员");
        }

        try {
            String safeRoomCode = normalizeRoomCodeStrict(roomCode);
            onlineChatRoomService.transferOwner(safeRoomCode, user.getUserId(), targetUserId, user.getLoginName());
            String targetName = resolveTargetUserName(safeRoomCode, targetUserId);
            recordOperation(safeRoomCode, "transfer_owner", user, targetUserId, targetName, "转让群主");
            return AjaxResult.success("群主转让成功");
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Transfer room owner failed, roomCode={}, targetUserId={}", roomCode, targetUserId, e);
            return AjaxResult.error(buildFriendlyMessage(e));
        }
    }

    @PostMapping("/room/delete")
    @ResponseBody
    public AjaxResult deleteRoom(String roomCode) {
        SysUser user = getSysUser();
        if (user == null || user.getUserId() == null) {
            return AjaxResult.error("请先登录");
        }

        try {
            String safeRoomCode = normalizeRoomCodeStrict(roomCode);
            if (PUBLIC_ROOM_CODE.equals(safeRoomCode)) {
                return AjaxResult.error("公共房间不允许删除");
            }
            if (!ROLE_OWNER.equals(onlineChatRoomService.selectMyRole(safeRoomCode, user.getUserId()))) {
                return AjaxResult.error("只有群主可以删除群聊");
            }
            onlineChatRoomService.deleteRoom(safeRoomCode, user.getUserId(), user.getLoginName());
            return AjaxResult.success("群聊已删除");
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Delete room failed, roomCode={}", roomCode, e);
            return AjaxResult.error(buildFriendlyMessage(e));
        }
    }

    @PostMapping("/room/mute")
    @ResponseBody
    public AjaxResult mute(String roomCode, Long targetUserId, Integer minutes, String reason) {
        SysUser user = getSysUser();
        if (user == null || user.getUserId() == null) {
            return AjaxResult.error("请先登录");
        }
        if (targetUserId == null) {
            return AjaxResult.error("请先选择目标成员");
        }
        if (targetUserId.equals(user.getUserId())) {
            return AjaxResult.error("不能对自己执行禁言");
        }

        try {
            String safeRoomCode = normalizeRoomCodeStrict(roomCode);
            String operatorRole = onlineChatRoomService.selectMyRole(safeRoomCode, user.getUserId());
            if (!isManagerRole(operatorRole)) {
                return AjaxResult.error("仅群主或管理员可执行禁言");
            }

            String targetRole = onlineChatRoomService.selectMyRole(safeRoomCode, targetUserId);
            if (ROLE_VISITOR.equals(targetRole)) {
                return AjaxResult.error("目标用户不在当前房间");
            }
            if (ROLE_OWNER.equals(targetRole)) {
                return AjaxResult.error("不能禁言群主");
            }
            if (ROLE_ADMIN.equals(operatorRole) && ROLE_ADMIN.equals(targetRole)) {
                return AjaxResult.error("管理员不能互相禁言");
            }

            int muteMinutes = normalizeMuteMinutes(minutes);
            Date now = DateUtils.getNowDate();
            Date endTime = new Date(now.getTime() + muteMinutes * 60L * 1000L);
            String safeReason = normalizeReason(reason);

            onlineChatMuteService.disableRoomMute(targetUserId, safeRoomCode, user.getLoginName());

            StudyOnlineChatMute mute = new StudyOnlineChatMute();
            mute.setUserId(targetUserId);
            mute.setRoomCode(safeRoomCode);
            mute.setStartTime(now);
            mute.setEndTime(endTime);
            mute.setStatus("0");
            mute.setReason(safeReason);
            mute.setCreateBy(user.getLoginName());
            mute.setCreateTime(now);
            mute.setUpdateBy(user.getLoginName());
            mute.setUpdateTime(now);
            onlineChatMuteService.insertStudyOnlineChatMute(mute);
            String targetName = resolveTargetUserName(safeRoomCode, targetUserId);
            recordOperation(safeRoomCode, "mute_user", user, targetUserId, targetName,
                    "禁言 " + muteMinutes + " 分钟，原因：" + safeReason);
            return AjaxResult.success("禁言成功，时长 " + muteMinutes + " 分钟");
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Mute room user failed, roomCode={}, targetUserId={}", roomCode, targetUserId, e);
            return AjaxResult.error(buildFriendlyMessage(e));
        }
    }

    @PostMapping("/room/unmute")
    @ResponseBody
    public AjaxResult unmute(String roomCode, Long targetUserId) {
        SysUser user = getSysUser();
        if (user == null || user.getUserId() == null) {
            return AjaxResult.error("请先登录");
        }
        if (targetUserId == null) {
            return AjaxResult.error("请先选择目标成员");
        }

        try {
            String safeRoomCode = normalizeRoomCodeStrict(roomCode);
            if (!onlineChatRoomService.canManageRoom(safeRoomCode, user.getUserId())) {
                return AjaxResult.error("仅群主或管理员可解除禁言");
            }
            int affected = onlineChatMuteService.disableRoomMute(targetUserId, safeRoomCode, user.getLoginName());
            if (affected > 0) {
                String targetName = resolveTargetUserName(safeRoomCode, targetUserId);
                recordOperation(safeRoomCode, "unmute_user", user, targetUserId, targetName, "解除禁言");
            }
            return affected > 0 ? AjaxResult.success("已解除禁言") : AjaxResult.error("目标用户当前没有生效禁言");
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Unmute room user failed, roomCode={}, targetUserId={}", roomCode, targetUserId, e);
            return AjaxResult.error(buildFriendlyMessage(e));
        }
    }

    private String buildFriendlyMessage(Exception e) {
        String message = e == null ? "" : String.valueOf(e.getMessage());
        if (message.contains("study_online_chat_message")
                || message.contains("study_online_chat_mute")
                || message.contains("study_online_chat_sensitive_word")
                || message.contains("study_online_chat_operation_log")
                || message.contains("study_online_chat_room")
                || message.contains("study_online_chat_room_user")
                || message.contains("doesn't exist")) {
            return "聊天室数据库表还没有初始化，请先执行 sql/add_online_chat_feature.sql";
        }
        return "聊天室历史消息暂时不可用，你仍然可以尝试重新连接。";
    }

    private String normalizeRoomCode(String roomCode) {
        if (roomCode == null || roomCode.trim().isEmpty()) {
            return PUBLIC_ROOM_CODE;
        }
        String trimmed = roomCode.trim();
        if (!trimmed.matches("^[a-zA-Z0-9_-]{1,64}$")) {
            return PUBLIC_ROOM_CODE;
        }
        return trimmed;
    }

    private String normalizeRoomCodeStrict(String roomCode) {
        if (StringUtils.isBlank(roomCode)) {
            throw new ServiceException("房间编码不能为空");
        }
        String safeRoomCode = roomCode.trim();
        if (!safeRoomCode.matches("^[a-zA-Z0-9_-]{1,64}$")) {
            throw new ServiceException("房间编码仅支持字母、数字、下划线和中划线，长度1-64");
        }
        return safeRoomCode;
    }

    private String normalizeRoomName(String roomName, String roomCode) {
        String safeName = StringUtils.defaultIfBlank(roomName, roomCode).trim();
        if (safeName.length() > 64) {
            safeName = safeName.substring(0, 64);
        }
        return safeName;
    }

    private int normalizeMuteMinutes(Integer minutes) {
        int safeMinutes = minutes == null ? 10 : minutes;
        if (safeMinutes < 1) {
            throw new ServiceException("禁言时长至少 1 分钟");
        }
        if (safeMinutes > 43200) {
            throw new ServiceException("禁言时长不能超过 43200 分钟（30天）");
        }
        return safeMinutes;
    }

    private String normalizeReason(String reason) {
        String safeReason = StringUtils.defaultIfBlank(reason, "管理员设置禁言").trim();
        if (safeReason.length() > 255) {
            safeReason = safeReason.substring(0, 255);
        }
        return safeReason;
    }

    private boolean isManagerRole(String role) {
        return ROLE_OWNER.equals(role) || ROLE_ADMIN.equals(role);
    }

    private void recordOperation(String roomCode, String actionType, SysUser operator,
                                 Long targetUserId, String targetUserName, String detail) {
        if (operator == null || operator.getUserId() == null || StringUtils.isBlank(roomCode) || StringUtils.isBlank(actionType)) {
            return;
        }
        try {
            StudyOnlineChatOperationLog log = new StudyOnlineChatOperationLog();
            log.setRoomCode(roomCode);
            log.setActionType(actionType);
            log.setOperatorUserId(operator.getUserId());
            log.setOperatorLoginName(StringUtils.defaultIfBlank(operator.getLoginName(), ""));
            log.setOperatorUserName(StringUtils.defaultIfBlank(operator.getUserName(), operator.getLoginName()));
            log.setTargetUserId(targetUserId);
            log.setTargetUserName(targetUserName);
            log.setDetail(normalizeDetail(detail));
            log.setCreateTime(DateUtils.getNowDate());
            onlineChatOperationLogService.insertStudyOnlineChatOperationLog(log);
        } catch (Exception e) {
            logger.warn("Record chat operation log failed, roomCode={}, actionType={}", roomCode, actionType, e);
        }
    }

    private String resolveTargetUserName(String roomCode, Long targetUserId) {
        if (targetUserId == null) {
            return null;
        }
        List<StudyOnlineChatRoomUser> roomUsers = onlineChatRoomService.selectEnabledRoomUsers(roomCode);
        if (StringUtils.isEmpty(roomUsers)) {
            return String.valueOf(targetUserId);
        }
        for (StudyOnlineChatRoomUser roomUser : roomUsers) {
            if (roomUser != null && targetUserId.equals(roomUser.getUserId())) {
                return StringUtils.defaultIfBlank(roomUser.getUserName(), roomUser.getLoginName());
            }
        }
        return String.valueOf(targetUserId);
    }

    private String normalizeDetail(String detail) {
        String safeDetail = StringUtils.defaultIfBlank(detail, "").trim();
        if (safeDetail.length() > 500) {
            safeDetail = safeDetail.substring(0, 500);
        }
        return safeDetail;
    }
}
