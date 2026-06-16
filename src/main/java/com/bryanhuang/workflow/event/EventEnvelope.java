package com.bryanhuang.workflow.event;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class EventEnvelope<T> {
    private UUID eventId;
    private EventType eventType;
    private Instant timestamp;
    private T payload;

    private EventEnvelope(
            UUID eventId,
            EventType eventType,
            Instant timestamp,
            T payload
    ) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.payload = payload;
    }
}
