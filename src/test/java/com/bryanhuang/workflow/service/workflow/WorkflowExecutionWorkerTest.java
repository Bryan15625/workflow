package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.exception.WorkflowExecutionNotFoundException;
import com.bryanhuang.workflow.repository.WorkflowExecutionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class WorkflowExecutionWorkerTest {

    @Mock
    private TaskExecutionService taskExecutionService;

    @Mock
    private WorkflowExecutionRepository workflowExecutionRepository;

    @InjectMocks
    private WorkflowExecutionWorker workflowExecutionWorker;

    @Test
    void executeWorkflow_shouldDelegateToQueryAndTaskExecutionServices() {
        // given
        UUID executionId = UUID.randomUUID();
        WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);

        when(workflowExecutionRepository.findById(executionId))
                .thenReturn(Optional.ofNullable(entity));

        // when
        workflowExecutionWorker.executeWorkflow(executionId);

        // then
        verify(workflowExecutionRepository).findById(executionId);
        verify(taskExecutionService).executeWorkflow(entity);
        verifyNoMoreInteractions(workflowExecutionRepository, taskExecutionService);
    }

    @Test
    void executeWorkflow_workflowNotFound() {
        // given
        UUID executionId = UUID.randomUUID();
        WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);

        when(workflowExecutionRepository.findById(executionId))
                .thenReturn(Optional.empty());

        // when
        WorkflowExecutionNotFoundException ex = assertThrows(
                WorkflowExecutionNotFoundException.class,
                () -> workflowExecutionWorker.executeWorkflow(executionId)
        );
        // then
        verify(workflowExecutionRepository).findById(executionId);
        verifyNoMoreInteractions(workflowExecutionRepository);
    }
}