package com.jy.study.web.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jy.study.common.exception.ServiceException;
import com.jy.study.common.utils.DateUtils;
import com.jy.study.common.utils.StringUtils;
import com.jy.study.lesson.domain.StudyOnlineChatMessage;
import com.jy.study.lesson.domain.StudyOnlineChatMute;
import com.jy.study.lesson.domain.StudyOnlineChatRoomUser;
import com.jy.study.lesson.domain.StudyOnlineChatSensitiveWord;
import com.jy.study.lesson.service.IStudyOnlineChatMessageService;
import com.jy.study.lesson.service.IStudyOnlineChatMuteService;
import com.jy.study.lesson.service.IStudyOnlineChatRoomService;
import com.jy.study.lesson.service.IStudyOnlineChatSensitiveWordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 在线聊天室 WebSocket 处理器
 */
@Component
public class OnlineChatWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(OnlineChatWebSocketHandler.class);

    private static final int MAX_CONTENT_LENGTH = 500;
    private static final int MAX_REPLY_SNIPPET_LENGTH = 120;
    private static final int SEND_TIME_LIMIT = 10 * 1000;
    private static final int BUFFER_SIZE_LIMIT = 64 * 1024;
    private static final int BACKFILL_LIMIT = 100;

    private static final int RATE_LIMIT_COUNT = 5;
    private static final long RATE_LIMIT_WINDOW_MS = 10_000L;
    private static final long RATE_LIMIT_CLEANUP_IDLE_MS = 120_000L;
    private static final long SENSITIVE_WORD_CACHE_MS = 30_000L;
    private static final int MAX_MENTION_PER_MESSAGE = 10;
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([\\u4e00-\\u9fa5A-Za-z0-9_-]{1,30})");

    private final Map<String, Map<String, WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final Map<String, Map<Long, Set<String>>> roomUserSessionIds = new ConcurrentHashMap<>();
    private final Map<String, UserRateWindow> userRateWindows = new ConcurrentHashMap<>();
    private final Object sensitiveWordCacheLock = new Object();

    private volatile List<StudyOnlineChatSensitiveWord> sensitiveWordsCache = Collections.emptyList();
    private volatile long sensitiveWordsCacheAt = 0L;

    @Autowired
    private IStudyOnlineChatMessageService onlineChatMessageService;

    @Autowired
    private IStudyOnlineChatMuteService onlineChatMuteService;

    @Autowired
    private IStudyOnlineChatSensitiveWordService onlineChatSensitiveWordService;

    @Autowired
    private IStudyOnlineChatRoomService onlineChatRoomService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        WebSocketSession concurrentSession = new ConcurrentWebSocketSessionDecorator(session, SEND_TIME_LIMIT, BUFFER_SIZE_LIMIT);
        ChatUser user = getChatUser(concurrentSession);
        String roomCode = user.getRoomCode();

        onlineChatRoomService.ensureRoomAndJoin(roomCode, user.getUserId(), user.getLoginName(), user.getUserName());
        getRoomSessions(roomCode).put(concurrentSession.getId(), concurrentSession);
        boolean firstConnection = addUserConnection(roomCode, user.getUserId(), concurrentSession.getId());

        sendInitMessage(concurrentSession, user);
        sendBackfillMessages(concurrentSession, user);

        if (firstConnection) {
            broadcastPresence(roomCode, "join", user);
        } else {
            broadcastOnlineCount(roomCode);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ChatUser user = getChatUser(session);
        cleanupRateWindowsIfNeeded();

        ChatClientMessage clientMessage;
        try {
            clientMessage = objectMapper.readValue(message.getPayload(), ChatClientMessage.class);
        } catch (IOException e) {
            sendError(session, "消息格式不正确");
            return;
        }

        if (clientMessage == null || !StringUtils.equalsIgnoreCase("chat", clientMessage.getType())) {
            sendError(session, "暂不支持该消息类型");
            return;
        }

        try {
            validateRoomAvailable(user);
            String content = normalizeContent(clientMessage.getContent());
            if (isRateLimited(user)) {
                throw new ServiceException("发送过快，请稍后再试");
            }
            validateMute(user);
            SensitiveFilterResult filterResult = filterSensitiveWords(content);
            List<StudyOnlineChatRoomUser> mentionedUsers =
                    resolveMentionedUsers(user.getRoomCode(), filterResult.getContent(), user.getUserId());
            StudyOnlineChatMessage replyMessage = resolveReplyMessage(user.getRoomCode(), clientMessage.getReplyMessageId());

            StudyOnlineChatMessage chatMessage = buildChatMessage(user, filterResult.getContent(), replyMessage);
            onlineChatMessageService.insertStudyOnlineChatMessage(chatMessage);
            broadcastChatMessage(user.getRoomCode(), chatMessage, mentionedUsers);

            if (filterResult.isFiltered()) {
                sendWarn(session, "消息包含敏感词，已自动替换后发送");
            }
        } catch (ServiceException e) {
            sendError(session, e.getMessage());
        } catch (Exception e) {
            log.error("Handle chat message failed, userId={}", user.getUserId(), e);
            sendError(session, buildFriendlyMessage(e));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.warn("Chat websocket transport error, sessionId={}", session.getId(), exception);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        ChatUser user = getChatUser(session);
        String roomCode = user.getRoomCode();

        Map<String, WebSocketSession> sessions = roomSessions.get(roomCode);
        if (sessions != null) {
            sessions.remove(session.getId());
            if (sessions.isEmpty()) {
                roomSessions.remove(roomCode);
            }
        }

        boolean userOffline = removeUserConnection(roomCode, user.getUserId(), session.getId());
        if (userOffline) {
            broadcastPresence(roomCode, "leave", user);
        } else {
            broadcastOnlineCount(roomCode);
        }
    }

    private void sendInitMessage(WebSocketSession session, ChatUser user) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "init");
        payload.put("roomCode", user.getRoomCode());
        payload.put("currentUserId", user.getUserId());
        payload.put("onlineCount", getOnlineUserCount(user.getRoomCode()));
        sendJson(session, payload);
    }

    private void sendBackfillMessages(WebSocketSession session, ChatUser user) throws IOException {
        if (user.getLastMessageId() <= 0) {
            return;
        }
        List<StudyOnlineChatMessage> backfillMessages =
                onlineChatMessageService.selectMessagesAfterId(user.getRoomCode(), user.getLastMessageId(), BACKFILL_LIMIT);
        if (StringUtils.isEmpty(backfillMessages)) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "backfill");
        payload.put("roomCode", user.getRoomCode());
        payload.put("onlineCount", getOnlineUserCount(user.getRoomCode()));

        List<Map<String, Object>> messages = new ArrayList<>();
        for (StudyOnlineChatMessage backfillMessage : backfillMessages) {
            messages.add(toMessageMap(backfillMessage));
        }
        payload.put("messages", messages);
        sendJson(session, payload);
    }

    private void broadcastChatMessage(String roomCode, StudyOnlineChatMessage chatMessage,
                                      List<StudyOnlineChatRoomUser> mentionedUsers) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "chat");
        payload.put("roomCode", roomCode);
        payload.put("onlineCount", getOnlineUserCount(roomCode));
        payload.put("message", toMessageMap(chatMessage, mentionedUsers));
        broadcastJson(roomCode, payload);
    }

    private void broadcastPresence(String roomCode, String action, ChatUser user) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "presence");
        payload.put("roomCode", roomCode);
        payload.put("action", action);
        payload.put("onlineCount", getOnlineUserCount(roomCode));
        payload.put("userName", user.getUserName());
        payload.put("createTime", DateUtils.getTime());
        payload.put("text", user.getUserName() + ("join".equals(action) ? " 进入了聊天室" : " 离开了聊天室"));
        broadcastJson(roomCode, payload);
    }

    private void broadcastOnlineCount(String roomCode) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "online");
        payload.put("roomCode", roomCode);
        payload.put("onlineCount", getOnlineUserCount(roomCode));
        broadcastJson(roomCode, payload);
    }

    private StudyOnlineChatMessage buildChatMessage(ChatUser user, String content, StudyOnlineChatMessage replyMessage) {
        StudyOnlineChatMessage chatMessage = new StudyOnlineChatMessage();
        chatMessage.setRoomCode(user.getRoomCode());
        chatMessage.setUserId(user.getUserId());
        chatMessage.setLoginName(user.getLoginName());
        chatMessage.setUserName(user.getUserName());
        if (replyMessage != null && replyMessage.getMessageId() != null) {
            chatMessage.setReplyMessageId(replyMessage.getMessageId());
            chatMessage.setReplyUserName(StringUtils.defaultIfBlank(replyMessage.getUserName(), replyMessage.getLoginName()));
            chatMessage.setReplyContent(buildReplySnippet(replyMessage.getContent()));
        }
        chatMessage.setContent(content);
        chatMessage.setCreateTime(DateUtils.getNowDate());
        return chatMessage;
    }

    private StudyOnlineChatMessage resolveReplyMessage(String roomCode, Long replyMessageId) {
        if (replyMessageId == null || replyMessageId <= 0) {
            return null;
        }
        StudyOnlineChatMessage replyMessage = onlineChatMessageService.selectMessageByIdInRoom(roomCode, replyMessageId);
        if (replyMessage == null || replyMessage.getMessageId() == null) {
            throw new ServiceException("回复的消息不存在或已删除");
        }
        return replyMessage;
    }

    private String buildReplySnippet(String content) {
        String snippet = StringUtils.defaultString(content)
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\n', ' ')
                .trim();
        if (snippet.length() > MAX_REPLY_SNIPPET_LENGTH) {
            snippet = snippet.substring(0, MAX_REPLY_SNIPPET_LENGTH) + "...";
        }
        return snippet;
    }

    private String normalizeContent(String content) {
        String normalized = StringUtils.defaultString(content)
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .trim();
        if (StringUtils.isBlank(normalized)) {
            throw new ServiceException("消息内容不能为空");
        }
        if (normalized.length() > MAX_CONTENT_LENGTH) {
            throw new ServiceException("单条消息不能超过" + MAX_CONTENT_LENGTH + "个字符");
        }
        return normalized;
    }

    private void validateMute(ChatUser user) {
        StudyOnlineChatMute mute = onlineChatMuteService.selectActiveMute(user.getUserId(), user.getRoomCode(), DateUtils.getNowDate());
        if (mute == null) {
            return;
        }
        String endTime = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, mute.getEndTime());
        String reason = StringUtils.defaultIfBlank(mute.getReason(), "管理员设置禁言");
        throw new ServiceException("你已被禁言至 " + endTime + "，原因：" + reason);
    }

    private void validateRoomAvailable(ChatUser user) {
        if (onlineChatRoomService.selectEnabledRoomByCode(user.getRoomCode()) == null) {
            throw new ServiceException("当前房间已被删除或关闭");
        }
    }

    private List<StudyOnlineChatRoomUser> resolveMentionedUsers(String roomCode, String content, Long senderUserId) {
        if (StringUtils.isBlank(content)) {
            return Collections.emptyList();
        }
        Set<String> mentionTokens = new LinkedHashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            String token = matcher.group(1);
            if (StringUtils.isBlank(token)) {
                continue;
            }
            mentionTokens.add(normalizeMentionToken(token));
            if (mentionTokens.size() >= MAX_MENTION_PER_MESSAGE) {
                break;
            }
        }
        if (mentionTokens.isEmpty()) {
            return Collections.emptyList();
        }

        List<StudyOnlineChatRoomUser> roomUsers = onlineChatRoomService.selectEnabledRoomUsers(roomCode);
        if (StringUtils.isEmpty(roomUsers)) {
            return Collections.emptyList();
        }
        Map<String, StudyOnlineChatRoomUser> roomUserIndex = new LinkedHashMap<>();
        for (StudyOnlineChatRoomUser roomUser : roomUsers) {
            if (roomUser == null || roomUser.getUserId() == null) {
                continue;
            }
            if (StringUtils.isNotBlank(roomUser.getUserName())) {
                roomUserIndex.putIfAbsent(normalizeMentionToken(roomUser.getUserName()), roomUser);
            }
            if (StringUtils.isNotBlank(roomUser.getLoginName())) {
                roomUserIndex.putIfAbsent(normalizeMentionToken(roomUser.getLoginName()), roomUser);
            }
        }

        Set<Long> addedUserIds = new HashSet<>();
        List<StudyOnlineChatRoomUser> result = new ArrayList<>();
        for (String token : mentionTokens) {
            StudyOnlineChatRoomUser targetUser = roomUserIndex.get(token);
            if (targetUser == null || targetUser.getUserId() == null) {
                continue;
            }
            if (senderUserId != null && senderUserId.equals(targetUser.getUserId())) {
                continue;
            }
            if (addedUserIds.add(targetUser.getUserId())) {
                result.add(targetUser);
            }
        }
        return result;
    }

    private String normalizeMentionToken(String token) {
        return StringUtils.defaultString(token).trim().toLowerCase(Locale.ROOT);
    }

    private SensitiveFilterResult filterSensitiveWords(String content) {
        String filtered = content;
        boolean changed = false;
        for (StudyOnlineChatSensitiveWord sensitiveWord : getSensitiveWords()) {
            if (sensitiveWord == null || StringUtils.isBlank(sensitiveWord.getWord())) {
                continue;
            }
            String replacement = StringUtils.defaultIfBlank(sensitiveWord.getReplaceText(), "**");
            String replaced = StringUtils.replaceIgnoreCase(filtered, sensitiveWord.getWord(), replacement);
            if (!StringUtils.equals(replaced, filtered)) {
                changed = true;
                filtered = replaced;
            }
        }
        return new SensitiveFilterResult(filtered, changed);
    }

    private List<StudyOnlineChatSensitiveWord> getSensitiveWords() {
        long now = System.currentTimeMillis();
        if (now - sensitiveWordsCacheAt < SENSITIVE_WORD_CACHE_MS) {
            return sensitiveWordsCache;
        }
        synchronized (sensitiveWordCacheLock) {
            if (now - sensitiveWordsCacheAt < SENSITIVE_WORD_CACHE_MS) {
                return sensitiveWordsCache;
            }
            try {
                List<StudyOnlineChatSensitiveWord> latestWords = onlineChatSensitiveWordService.selectEnabledWords();
                sensitiveWordsCache = latestWords == null ? Collections.emptyList() : latestWords;
            } catch (Exception e) {
                log.warn("Load sensitive words failed, keep old cache.", e);
                if (sensitiveWordsCache == null) {
                    sensitiveWordsCache = Collections.emptyList();
                }
            }
            sensitiveWordsCacheAt = now;
            return sensitiveWordsCache;
        }
    }

    private boolean isRateLimited(ChatUser user) {
        long now = System.currentTimeMillis();
        String key = user.getRoomCode() + ":" + user.getUserId();
        UserRateWindow window = userRateWindows.computeIfAbsent(key, unused -> new UserRateWindow());
        synchronized (window) {
            while (!window.timestamps.isEmpty() && now - window.timestamps.peekFirst() > RATE_LIMIT_WINDOW_MS) {
                window.timestamps.pollFirst();
            }
            if (window.timestamps.size() >= RATE_LIMIT_COUNT) {
                window.lastAccessAt = now;
                return true;
            }
            window.timestamps.offerLast(now);
            window.lastAccessAt = now;
            return false;
        }
    }

    private void cleanupRateWindowsIfNeeded() {
        if (userRateWindows.size() < 1000) {
            return;
        }
        long now = System.currentTimeMillis();
        for (Map.Entry<String, UserRateWindow> entry : userRateWindows.entrySet()) {
            UserRateWindow window = entry.getValue();
            if (window == null) {
                continue;
            }
            if (now - window.lastAccessAt > RATE_LIMIT_CLEANUP_IDLE_MS) {
                userRateWindows.remove(entry.getKey());
            }
        }
    }

    private Map<String, Object> toMessageMap(StudyOnlineChatMessage message) {
        return toMessageMap(message, Collections.emptyList());
    }

    private Map<String, Object> toMessageMap(StudyOnlineChatMessage message, List<StudyOnlineChatRoomUser> mentionedUsers) {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("messageId", message.getMessageId());
        messageMap.put("roomCode", message.getRoomCode());
        messageMap.put("userId", message.getUserId());
        messageMap.put("loginName", message.getLoginName());
        messageMap.put("userName", message.getUserName());
        messageMap.put("replyMessageId", message.getReplyMessageId());
        messageMap.put("replyUserName", message.getReplyUserName());
        messageMap.put("replyContent", message.getReplyContent());
        messageMap.put("content", message.getContent());
        messageMap.put("createTime", DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, message.getCreateTime()));
        if (StringUtils.isNotEmpty(mentionedUsers)) {
            List<Long> mentionUserIds = new ArrayList<>();
            List<String> mentionUserNames = new ArrayList<>();
            for (StudyOnlineChatRoomUser mentionedUser : mentionedUsers) {
                if (mentionedUser == null || mentionedUser.getUserId() == null) {
                    continue;
                }
                mentionUserIds.add(mentionedUser.getUserId());
                mentionUserNames.add(StringUtils.defaultIfBlank(mentionedUser.getUserName(), mentionedUser.getLoginName()));
            }
            messageMap.put("mentionUserIds", mentionUserIds);
            messageMap.put("mentionUserNames", mentionUserNames);
        }
        return messageMap;
    }

    private void sendWarn(WebSocketSession session, String message) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "warn");
        payload.put("message", message);
        sendJson(session, payload);
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "error");
        payload.put("message", message);
        sendJson(session, payload);
    }

    private void broadcastJson(String roomCode, Map<String, Object> payload) throws IOException {
        Map<String, WebSocketSession> sessions = roomSessions.get(roomCode);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        for (WebSocketSession session : sessions.values()) {
            if (!session.isOpen()) {
                continue;
            }
            try {
                sendJson(session, payload);
            } catch (IOException e) {
                log.warn("Broadcast chat message failed, sessionId={}", session.getId(), e);
                closeQuietly(session);
            }
        }
    }

    private void sendJson(WebSocketSession session, Map<String, Object> payload) throws IOException {
        if (session == null || !session.isOpen()) {
            return;
        }
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
    }

    private boolean addUserConnection(String roomCode, Long userId, String sessionId) {
        Set<String> sessionIds = getRoomUserSessionIds(roomCode).computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet());
        return sessionIds.add(sessionId) && sessionIds.size() == 1;
    }

    private boolean removeUserConnection(String roomCode, Long userId, String sessionId) {
        Map<Long, Set<String>> roomUserSessions = roomUserSessionIds.get(roomCode);
        if (roomUserSessions == null) {
            return false;
        }
        Set<String> sessionIds = roomUserSessions.get(userId);
        if (sessionIds == null) {
            return false;
        }
        sessionIds.remove(sessionId);
        if (sessionIds.isEmpty()) {
            roomUserSessions.remove(userId);
            if (roomUserSessions.isEmpty()) {
                roomUserSessionIds.remove(roomCode);
            }
            return true;
        }
        return false;
    }

    private int getOnlineUserCount(String roomCode) {
        Map<Long, Set<String>> roomUserSessions = roomUserSessionIds.get(roomCode);
        return roomUserSessions == null ? 0 : roomUserSessions.size();
    }

    private Map<String, WebSocketSession> getRoomSessions(String roomCode) {
        return roomSessions.computeIfAbsent(roomCode, key -> new ConcurrentHashMap<>());
    }

    private Map<Long, Set<String>> getRoomUserSessionIds(String roomCode) {
        return roomUserSessionIds.computeIfAbsent(roomCode, key -> new ConcurrentHashMap<>());
    }

    private void closeQuietly(WebSocketSession session) {
        try {
            if (session != null && session.isOpen()) {
                session.close(CloseStatus.SERVER_ERROR);
            }
        } catch (IOException e) {
            log.debug("Close websocket session failed, sessionId={}", session.getId(), e);
        }
    }

    private ChatUser getChatUser(WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        Object userId = attributes.get(OnlineChatHandshakeInterceptor.ATTR_USER_ID);
        Object loginName = attributes.get(OnlineChatHandshakeInterceptor.ATTR_LOGIN_NAME);
        Object userName = attributes.get(OnlineChatHandshakeInterceptor.ATTR_USER_NAME);
        Object roomCode = attributes.get(OnlineChatHandshakeInterceptor.ATTR_ROOM_CODE);
        Object lastMessageId = attributes.get(OnlineChatHandshakeInterceptor.ATTR_LAST_MESSAGE_ID);
        if (!(userId instanceof Long) || !(loginName instanceof String) || !(userName instanceof String)) {
            throw new ServiceException("当前登录状态已失效，请刷新页面后重试");
        }
        String safeRoomCode = roomCode instanceof String ? (String) roomCode : OnlineChatHandshakeInterceptor.PUBLIC_ROOM_CODE;
        long safeLastMessageId = lastMessageId instanceof Long ? (Long) lastMessageId : 0L;
        return new ChatUser((Long) userId, (String) loginName, (String) userName, safeRoomCode, safeLastMessageId);
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
        return "聊天室暂时不可用，请稍后再试";
    }

    private static class ChatClientMessage {
        private String type;
        private String content;
        private Long replyMessageId;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Long getReplyMessageId() {
            return replyMessageId;
        }

        public void setReplyMessageId(Long replyMessageId) {
            this.replyMessageId = replyMessageId;
        }
    }

    private static class ChatUser {
        private final Long userId;
        private final String loginName;
        private final String userName;
        private final String roomCode;
        private final long lastMessageId;

        private ChatUser(Long userId, String loginName, String userName, String roomCode, long lastMessageId) {
            this.userId = userId;
            this.loginName = loginName;
            this.userName = userName;
            this.roomCode = roomCode;
            this.lastMessageId = lastMessageId;
        }

        public Long getUserId() {
            return userId;
        }

        public String getLoginName() {
            return loginName;
        }

        public String getUserName() {
            return userName;
        }

        public String getRoomCode() {
            return roomCode;
        }

        public long getLastMessageId() {
            return lastMessageId;
        }
    }

    private static class SensitiveFilterResult {
        private final String content;
        private final boolean filtered;

        private SensitiveFilterResult(String content, boolean filtered) {
            this.content = content;
            this.filtered = filtered;
        }

        public String getContent() {
            return content;
        }

        public boolean isFiltered() {
            return filtered;
        }
    }

    private static class UserRateWindow {
        private final Deque<Long> timestamps = new ArrayDeque<>();
        private volatile long lastAccessAt = System.currentTimeMillis();
    }
}
