package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.entity.WorkflowEntity;
import com.bryanhuang.workflow.exception.WorkflowNotFoundException;
import com.bryanhuang.workflow.mapper.WorkflowEntityMapper;
import com.bryanhuang.workflow.model.workflow.Workflow;
import com.bryanhuang.workflow.repository.WorkflowRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowQueryServiceTest {

    @Mock
    private WorkflowEntityMapper workflowEntityMapper;

    @Mock
    private WorkflowRepository workflowRepository;

    @InjectMocks
    private WorkflowQueryService workflowQueryService;

    @Test
    @DisplayName("Workflow entity found and mapped to workflow (happy path)")
    void workflowEntityFoundAndMappedToWorkflow() {
        UUID workflowId = UUID.randomUUID();
        Workflow workflow = Workflow.builder().build();

        WorkflowEntity entity = new WorkflowEntity();
        when(workflowRepository.findById(workflowId))
                .thenReturn(Optional.of(entity));

        when(workflowEntityMapper.toWorkflow(entity))
                .thenReturn(workflow);

        workflowQueryService.findWorkflowEntityAndMapToWorkflow(workflowId);

        verify(workflowRepository).findById(workflowId);
        verify(workflowEntityMapper).toWorkflow(entity);
        verifyNoMoreInteractions(workflowEntityMapper, workflowRepository);
    }

    @Test
    @DisplayName("Workflow entity not found and exception is thrown")
    void workflowEntityNotFoundAndExceptionIsThrown() {
        UUID workflowId = UUID.randomUUID();

        when(workflowRepository.findById(workflowId))
                .thenReturn(Optional.empty());

        WorkflowNotFoundException ex = assertThrows(
                WorkflowNotFoundException.class,
                () -> workflowQueryService.findWorkflowEntityAndMapToWorkflow(workflowId)
        );
        assertEquals("Workflow not found with id: " + workflowId, ex.getMessage());

        verify(workflowRepository).findById(workflowId);
        verifyNoMoreInteractions(workflowRepository);
    }
}
