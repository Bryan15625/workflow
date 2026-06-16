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
    public <T> void handle(EventEnvelope<T> envelope) {

    }
}
