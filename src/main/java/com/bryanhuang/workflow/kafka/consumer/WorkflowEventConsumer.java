package com.bryanhuang.workflow.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class WorkflowEventConsumer {

    @KafkaListener(topics = "workflow-events", groupId = "workflow-group")
    public void consume(String event) {
        log.info("Received event: {}", event);
    }
}
