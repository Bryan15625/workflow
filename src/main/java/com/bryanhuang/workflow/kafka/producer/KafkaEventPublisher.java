package com.bryanhuang.workflow.kafka.producer;

import com.bryanhuang.workflow.event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import org.springframework.kafka.core.KafkaTemplate;
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {

    private final KafkaTemplate<String, EventEnvelope<?>> kafkaTemplate;

    public <T> void publish(String key, EventEnvelope<T> event) {
        log.info("Publishing event: {}", event);
        kafkaTemplate.send(
                "workflow-events",
                key,
                event
        );
    }
}