package com.bryanhuang.workflow.kafka.consumer;

import com.bryanhuang.workflow.event.EventEnvelope;
import com.bryanhuang.workflow.event.handler.WorkflowExecutionEventHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class WorkflowExecutionConsumerTest {

    @Mock
    private WorkflowExecutionEventHandler workflowExecutionEventHandler;

    @InjectMocks
    private WorkflowExecutionConsumer workflowExecutionConsumer;

    @Test
    void consume_shouldDelegateToWorkflowExecutionEventHandler() {
        // given
        @SuppressWarnings("unchecked")
        EventEnvelope<?> event = mock(EventEnvelope.class);

        // when
        workflowExecutionConsumer.consume(event);

        // then
        verify(workflowExecutionEventHandler).handle(event);
        verifyNoMoreInteractions(workflowExecutionEventHandler);
    }
}