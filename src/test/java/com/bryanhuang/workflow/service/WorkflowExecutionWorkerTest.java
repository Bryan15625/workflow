package com.bryanhuang.workflow.service;

import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class WorkflowExecutionWorkerTest {

    @Mock
    private TaskExecutionService taskExecutionService;

    @Mock
    private WorkflowQueryService workflowQueryService;

    @InjectMocks
    private WorkflowExecutionWorker workflowExecutionWorker;

    @Test
    void executeWorkflow_shouldDelegateToQueryAndTaskExecutionServices() {
        // given
        UUID executionId = UUID.randomUUID();
        WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);

        when(workflowQueryService.getWorkflowExecutionEntityById(executionId))
                .thenReturn(entity);

        // when
        workflowExecutionWorker.executeWorkflow(executionId);

        // then
        verify(workflowQueryService).getWorkflowExecutionEntityById(executionId);
        verify(taskExecutionService).executeWorkflow(entity);
        verifyNoMoreInteractions(workflowQueryService, taskExecutionService);
    }
}