package com.test.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.event.NewOrderEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MessageServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private WebSocket webSocket;
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        webSocket = mock(WebSocket.class);
        messageService = new MessageService();
        ReflectionTestUtils.setField(messageService, "webSocket", webSocket);
        ReflectionTestUtils.setField(messageService, "objectMapper", objectMapper);
    }

    @Test
    void addsConsumedAtAndKeepsStructuredFields() throws Exception {
        NewOrderEvent event = new NewOrderEvent();
        event.setEventId("event-1");
        event.setOrderId("order-1");
        event.setTestRunId("run-1");
        event.setSentAt(1000L);

        long before = System.currentTimeMillis();
        messageService.onMessage(objectMapper.writeValueAsString(event));
        long after = System.currentTimeMillis();

        ArgumentCaptor<String> structured = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> legacy = ArgumentCaptor.forClass(String.class);
        verify(webSocket).sendMessage(structured.capture(), legacy.capture());
        NewOrderEvent delivered = objectMapper.readValue(structured.getValue(), NewOrderEvent.class);
        assertEquals("event-1", delivered.getEventId());
        assertEquals("order-1", delivered.getOrderId());
        assertEquals("run-1", delivered.getTestRunId());
        assertEquals(Long.valueOf(1000L), delivered.getSentAt());
        assertNotNull(delivered.getConsumedAt());
        assertTrue(delivered.getConsumedAt() >= before && delivered.getConsumedAt() <= after);
        assertEquals("有新的订单", legacy.getValue());
    }

    @Test
    void forwardsLegacyMessagesUnchanged() {
        messageService.onMessage("有新的订单");
        verify(webSocket).sendMessage("有新的订单", "有新的订单");
    }
}
