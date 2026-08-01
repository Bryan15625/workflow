package com.bryanhuang.workflow.event.handler;

import com.bryanhuang.workflow.event.EventEnvelope;
import com.bryanhuang.workflow.event.EventType;
import com.bryanhuang.workflow.event.execution.WorkflowExecutionEvent;
import com.bryanhuang.workflow.service.workflow.WorkflowExecutionOrchestratorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionEventHandler {

    private final WorkflowExecutionOrchestratorService workflowExecutionOrchestratorService;
    private final ObjectMapper objectMapper;

    public void handle(EventEnvelope<?> envelope) {
        // if eventid already processed, skip
        // if execution is already in the right state, skip (indepotentecy)
        log.info("handling event: {}", envelope);
        EventType eventType = envelope.getEventType();
        if (eventType == null) {
            throw new NullPointerException("Event type cannot be null");
        } else if (eventType == EventType.WORKFLOW_EXECUTION_CREATED) {
            WorkflowExecutionEvent event = objectMapper.convertValue(
                    envelope.getPayload(),
                    WorkflowExecutionEvent.class
            );
            workflowExecutionOrchestratorService.onCreated(event);
        } else {
            log.info("No handler for event type: {} yet", eventType);
        }

    }
}
