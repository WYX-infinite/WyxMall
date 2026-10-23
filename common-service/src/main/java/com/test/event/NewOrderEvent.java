package com.test.event;

import java.io.Serializable;

/**
 * Structured event used to measure the new-order notification path.
 */
public class NewOrderEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type = "NEW_ORDER";
    private String message = "有新的订单";
    private String eventId;
    private String orderId;
    private String testRunId;
    private Long sentAt;
    private Long consumedAt;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTestRunId() {
        return testRunId;
    }

    public void setTestRunId(String testRunId) {
        this.testRunId = testRunId;
    }

    public Long getSentAt() {
        return sentAt;
    }

    public void setSentAt(Long sentAt) {
        this.sentAt = sentAt;
    }

    public Long getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(Long consumedAt) {
        this.consumedAt = consumedAt;
    }
}
