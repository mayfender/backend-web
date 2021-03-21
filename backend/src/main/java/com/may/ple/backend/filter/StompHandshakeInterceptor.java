package com.may.ple.backend.filter;

import java.util.Map;

import javax.annotation.Nullable;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

public class StompHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest req, ServerHttpResponse resp, WebSocketHandler h, Map<String, Object> atts) {
        System.out.println();
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest rq, ServerHttpResponse rp, WebSocketHandler h, @Nullable Exception e) {}
}