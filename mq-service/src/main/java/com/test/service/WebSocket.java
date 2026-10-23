package com.test.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/webSocket")
@Slf4j
public class WebSocket {

    private Session session;
    private boolean structuredFormat;
    private final Object sendLock = new Object();

    private static CopyOnWriteArraySet<WebSocket> webSocketSet = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session){
        this.session = session;
        this.structuredFormat = hasStructuredFormat(session.getQueryString());
        webSocketSet.add(this);
        log.info("【websocket消息】有新的连接，总数：{}",webSocketSet.size());
    }

    @OnClose
    public void onClose(){
        webSocketSet.remove(this);
        log.info("【websocket】连接断开，总数：{}",webSocketSet.size());
    }

    @OnMessage
    public void onMessage(String message){
        log.info("【websocket】收到客户端发来的消息：{}",message);
    }

    public void sendMessage(String message){
        sendMessage(message, message);
    }

    public void sendMessage(String structuredMessage, String legacyMessage){
        for(WebSocket webSocket:webSocketSet){
            String outgoingMessage = webSocket.structuredFormat ? structuredMessage : legacyMessage;
            log.info("【websocket消息】广播消息，structured={}", webSocket.structuredFormat);
            try {
                // RocketMQ consumes concurrently; BasicRemote does not permit overlapping writes.
                synchronized (webSocket.sendLock) {
                    if (webSocket.session.isOpen()) {
                        webSocket.session.getBasicRemote().sendText(outgoingMessage);
                    }
                }
            } catch (IOException | IllegalStateException e) {
                log.warn("【websocket消息】广播失败，sessionId={}", webSocket.session.getId(), e);
            }
        }
    }

    private boolean hasStructuredFormat(String queryString) {
        if (queryString == null) {
            return false;
        }
        for (String parameter : queryString.split("&")) {
            if ("format=structured".equalsIgnoreCase(parameter)) {
                return true;
            }
        }
        return false;
    }

}
