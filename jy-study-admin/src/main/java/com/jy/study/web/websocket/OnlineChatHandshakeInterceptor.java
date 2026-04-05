package com.jy.study.web.websocket;

import com.jy.study.common.core.domain.entity.SysUser;
import com.jy.study.common.utils.ShiroUtils;
import com.jy.study.common.utils.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * 聊天室握手拦截器
 */
@Component
public class OnlineChatHandshakeInterceptor implements HandshakeInterceptor {
    static final String PUBLIC_ROOM_CODE = "public-room";
    static final String ATTR_USER_ID = "chatUserId";
    static final String ATTR_LOGIN_NAME = "chatLoginName";
    static final String ATTR_USER_NAME = "chatUserName";
    static final String ATTR_ROOM_CODE = "chatRoomCode";
    static final String ATTR_LAST_MESSAGE_ID = "chatLastMessageId";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        SysUser user = ShiroUtils.getSysUser();
        if (user == null || user.getUserId() == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        attributes.put(ATTR_USER_ID, user.getUserId());
        attributes.put(ATTR_LOGIN_NAME, user.getLoginName());
        attributes.put(ATTR_USER_NAME, resolveDisplayName(user));
        attributes.put(ATTR_ROOM_CODE, resolveRoomCode(request));
        attributes.put(ATTR_LAST_MESSAGE_ID, resolveLastMessageId(request));
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    private String resolveDisplayName(SysUser user) {
        if (StringUtils.isNotEmpty(user.getUserName())) {
            return user.getUserName();
        }
        return user.getLoginName();
    }

    private String resolveRoomCode(ServerHttpRequest request) {
        String roomCode = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("roomCode");
        if (StringUtils.isBlank(roomCode)) {
            return PUBLIC_ROOM_CODE;
        }
        roomCode = roomCode.trim();
        if (!roomCode.matches("^[a-zA-Z0-9_-]{1,64}$")) {
            return PUBLIC_ROOM_CODE;
        }
        return roomCode;
    }

    private Long resolveLastMessageId(ServerHttpRequest request) {
        String lastMessageId = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("lastMessageId");
        if (StringUtils.isBlank(lastMessageId)) {
            return 0L;
        }
        try {
            long value = Long.parseLong(lastMessageId.trim());
            return Math.max(value, 0L);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
