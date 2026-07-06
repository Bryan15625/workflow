package com.bryanhuang.workflow.event.handler;

import com.bryanhuang.workflow.event.EventEnvelope;
import com.bryanhuang.workflow.event.execution.WorkflowExecutionEvent;
import com.bryanhuang.workflow.service.WorkflowExecutionOrchestratorService;
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
        switch (envelope.getEventType()) {
            case WORKFLOW_EXECUTION_CREATED -> {
                log.info("Handling WORKFLOW_EXECUTION_CREATED event");
                WorkflowExecutionEvent event = objectMapper.convertValue(
                        envelope.getPayload(),
                        WorkflowExecutionEvent.class
                );
                workflowExecutionOrchestratorService.onCreated(event);
            }
            case WORKFLOW_EXECUTION_STARTED ->
                log.info("Handling WORKFLOW_EXECUTION_STARTED event");
            case WORKFLOW_EXECUTION_COMPLETED ->
                log.info("Handling WORKFLOW_EXECUTION_COMPLETED event");
            case WORKFLOW_EXECUTION_FAILED ->
                log.info("Handling WORKFLOW_EXECUTION_FAILED event");
            case STEP_STARTED ->
                log.info("Handling STEP_STARTED event");
            case STEP_COMPLETED ->
                log.info("Handling STEP_COMPLETED event");
            case STEP_FAILED ->
                log.info("Handling STEP_FAILED event");

        }

    }
}
