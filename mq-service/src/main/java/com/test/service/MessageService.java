package com.test.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.event.NewOrderEvent;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RocketMQMessageListener(consumerGroup = "message",topic = "myTopic")
public class MessageService implements RocketMQListener<String> {
    @Autowired
    private WebSocket webSocket;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(String message) {
        long consumedAt = System.currentTimeMillis();
        String structuredMessage;
        String legacyMessage;
        try {
            NewOrderEvent event = objectMapper.readValue(message, NewOrderEvent.class);
            if (event.getEventId() == null || event.getOrderId() == null || event.getSentAt() == null) {
                throw new IllegalArgumentException("不是可识别的新订单事件");
            }
            event.setConsumedAt(consumedAt);
            structuredMessage = objectMapper.writeValueAsString(event);
            legacyMessage = event.getMessage();
        } catch (Exception ignored) {
            // 兼容升级前已在 RocketMQ 中的纯文本消息。
            structuredMessage = message;
            legacyMessage = message;
        }
        // Keep WebSocket delivery outside the compatibility parsing branch. A send failure must
        // not cause the same MQ message to be sent again without consumedAt.
        webSocket.sendMessage(structuredMessage, legacyMessage);
    }
}
