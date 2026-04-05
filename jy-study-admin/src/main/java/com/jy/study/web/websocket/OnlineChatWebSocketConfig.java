package com.jy.study.web.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 在线聊天室 WebSocket 配置
 */
@Configuration
@EnableWebSocket
public class OnlineChatWebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private OnlineChatHandshakeInterceptor onlineChatHandshakeInterceptor;

    @Autowired
    private OnlineChatWebSocketHandler onlineChatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(onlineChatWebSocketHandler, "/web/chat/ws")
                .addInterceptors(onlineChatHandshakeInterceptor);
    }
}
