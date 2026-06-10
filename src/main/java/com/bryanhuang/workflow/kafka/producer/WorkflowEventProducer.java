package com.bryanhuang.workflow.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import org.springframework.kafka.core.KafkaTemplate;
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publish(String event) {
        log.info("Publishing event: {}", event);
        kafkaTemplate.send("workflow-events", event);
    }
}