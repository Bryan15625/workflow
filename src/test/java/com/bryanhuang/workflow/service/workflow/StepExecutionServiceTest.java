package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.entity.StepExecutionStatusEntity;
import com.bryanhuang.workflow.exception.StepExecutionStatusNotFoundException;
import com.bryanhuang.workflow.repository.StepExecutionStatusRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StepExecutionServiceTest {

    @Mock
    private StepExecutionStatusRepository stepExecutionStatusRepository;

    @InjectMocks
    private StepExecutionService stepExecutionService;

    @Nested
    @DisplayName("start()")
    class StartTests {

        @Test
        @DisplayName("should call start() on found step")
        void startCallsEntityStartWhenStepFound() {
            UUID workflowExecutionId = UUID.randomUUID();
            Integer stepId = 1;
            StepExecutionStatusEntity stepEntity = mock(StepExecutionStatusEntity.class);

            when(stepExecutionStatusRepository
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId))
                    .thenReturn(Optional.of(stepEntity));

            stepExecutionService.start(workflowExecutionId, stepId);

            verify(stepExecutionStatusRepository)
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId);
            verify(stepEntity).start();
            verifyNoMoreInteractions(stepEntity);
        }

        @Test
        @DisplayName("should throw StepExecutionStatusNotFoundException when step not found")
        void startThrowsWhenStepNotFound() {
            UUID workflowExecutionId = UUID.randomUUID();
            Integer stepId = 1;

            when(stepExecutionStatusRepository
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    StepExecutionStatusNotFoundException.class,
                    () -> stepExecutionService.start(workflowExecutionId, stepId)
            );

            verify(stepExecutionStatusRepository)
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId);
        }
    }

    @Nested
    @DisplayName("complete()")
    class CompleteTests {

        @Test
        @DisplayName("should call complete() on found step")
        void completeCallsEntityCompleteWhenStepFound() {
            UUID workflowExecutionId = UUID.randomUUID();
            Integer stepId = 2;
            StepExecutionStatusEntity stepEntity = mock(StepExecutionStatusEntity.class);

            when(stepExecutionStatusRepository
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId))
                    .thenReturn(Optional.of(stepEntity));

            stepExecutionService.complete(workflowExecutionId, stepId);

            verify(stepExecutionStatusRepository)
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId);
            verify(stepEntity).complete();
            verifyNoMoreInteractions(stepEntity);
        }

        @Test
        @DisplayName("should throw StepExecutionStatusNotFoundException when step not found")
        void completeThrowsWhenStepNotFound() {
            UUID workflowExecutionId = UUID.randomUUID();
            Integer stepId = 2;

            when(stepExecutionStatusRepository
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    StepExecutionStatusNotFoundException.class,
                    () -> stepExecutionService.complete(workflowExecutionId, stepId)
            );

            verify(stepExecutionStatusRepository)
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId);
        }
    }

    @Nested
    @DisplayName("fail()")
    class FailTests {

        @Test
        @DisplayName("should call fail() on found step")
        void failCallsEntityFailWhenStepFound() {
            UUID workflowExecutionId = UUID.randomUUID();
            Integer stepId = 3;
            StepExecutionStatusEntity stepEntity = mock(StepExecutionStatusEntity.class);

            when(stepExecutionStatusRepository
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId))
                    .thenReturn(Optional.of(stepEntity));

            stepExecutionService.fail(workflowExecutionId, stepId);

            verify(stepExecutionStatusRepository)
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId);
            verify(stepEntity).fail();
            verifyNoMoreInteractions(stepEntity);
        }

        @Test
        @DisplayName("should throw StepExecutionStatusNotFoundException when step not found")
        void failThrowsWhenStepNotFound() {
            UUID workflowExecutionId = UUID.randomUUID();
            Integer stepId = 3;

            when(stepExecutionStatusRepository
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    StepExecutionStatusNotFoundException.class,
                    () -> stepExecutionService.fail(workflowExecutionId, stepId)
            );

            verify(stepExecutionStatusRepository)
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId);
        }
    }

}