package com.bryanhuang.workflow.kafka.consumer;

import com.bryanhuang.workflow.event.EventEnvelope;
import com.bryanhuang.workflow.event.handler.WorkflowExecutionEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class WorkflowExecutionConsumer {

    private final WorkflowExecutionEventHandler workflowExecutionEventHandler;

    @KafkaListener(
            topics = "workflow-events",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(EventEnvelope<?> event) {
        log.info("Received event: {}", event);
        workflowExecutionEventHandler.handle(event);
    }

}
