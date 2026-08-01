package com.bryanhuang.workflow.event.handler;

import com.bryanhuang.workflow.event.EventEnvelope;
import com.bryanhuang.workflow.event.EventType;
import com.bryanhuang.workflow.event.execution.WorkflowExecutionEvent;
import com.bryanhuang.workflow.service.workflow.WorkflowExecutionOrchestratorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowExecutionEventHandlerTest {

    @Mock
    private WorkflowExecutionOrchestratorService orchestratorService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WorkflowExecutionEventHandler handler;

    @Test
    @DisplayName("handle() converts payload and invokes orchestrator.onCreated for WORKFLOW_EXECUTION_CREATED")
    void handle_workflowExecutionCreated_callsOnCreated() {
        WorkflowExecutionEvent payload = new WorkflowExecutionEvent();
        EventEnvelope<WorkflowExecutionEvent> envelope = mock(EventEnvelope.class);
        WorkflowExecutionEvent event = mock(WorkflowExecutionEvent.class);

        when(envelope.getEventType()).thenReturn(EventType.WORKFLOW_EXECUTION_CREATED);
        when(envelope.getPayload()).thenReturn(payload);
        when(objectMapper.convertValue(payload, WorkflowExecutionEvent.class)).thenReturn(event);

        handler.handle(envelope);

        verify(objectMapper).convertValue(payload, WorkflowExecutionEvent.class);
        verify(orchestratorService).onCreated(event);
        verifyNoMoreInteractions(orchestratorService, objectMapper);
    }

    @Test
    @DisplayName("handle() does not call orchestrator.onCreated for non-CREATED events")
    void handle_nonCreatedEvents_doesNotCallOnCreated() {
        EventEnvelope<WorkflowExecutionEvent> envelope = mock(EventEnvelope.class);

        for (EventType eventType : EventType.values()) {
            if (eventType == EventType.WORKFLOW_EXECUTION_CREATED) continue;
            when(envelope.getEventType()).thenReturn(eventType);
            handler.handle(envelope);
        }

        verify(orchestratorService, never()).onCreated(any());
        verify(objectMapper, never()).convertValue(any(), eq(WorkflowExecutionEvent.class));
        verifyNoMoreInteractions(orchestratorService, objectMapper);
    }

    @Test
    void handle_nullEventType_shouldThrowNullPointerException() {
        // given
        EventEnvelope<?> envelope = mock(EventEnvelope.class);
        when(envelope.getEventType()).thenReturn(null);

        // when / then
        assertThrows(
                NullPointerException.class,
                () -> handler.handle(envelope)
        );
    }
}