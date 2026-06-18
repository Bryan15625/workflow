package com.bryanhuang.workflow.event.handler;

import com.bryanhuang.workflow.event.EventEnvelope;
import com.bryanhuang.workflow.service.WorkflowExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionEventHandler {

    private WorkflowExecutionService workflowExecutionService;

    public void handle(EventEnvelope<?> envelope) {
        // if eventid already processed, skip
        // if execution is already in the right state, skip (indepotentecy)
        log.info("handling event: {}", envelope);

    }
}
