package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.entity.workflow.StepExecutionStatusEntity;
import com.bryanhuang.workflow.exception.StepExecutionStatusNotFoundException;
import com.bryanhuang.workflow.model.workflow.JobStatus;
import com.bryanhuang.workflow.repository.StepExecutionStatusRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
    @DisplayName("pause()")
    class PauseTests {

        @Test
        @DisplayName("should call pause() on found step")
        void pauseCallsEntityPauseWhenStepFound() {
            UUID workflowExecutionId = UUID.randomUUID();
            Integer stepId = 4;
            StepExecutionStatusEntity stepEntity = mock(StepExecutionStatusEntity.class);

            when(stepExecutionStatusRepository
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId))
                    .thenReturn(Optional.of(stepEntity));

            stepExecutionService.pause(workflowExecutionId, stepId);

            verify(stepExecutionStatusRepository)
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId);
            verify(stepEntity).pause();
            verifyNoMoreInteractions(stepEntity);
        }

        @Test
        @DisplayName("should throw StepExecutionStatusNotFoundException when step not found")
        void pauseThrowsWhenStepNotFound() {
            UUID workflowExecutionId = UUID.randomUUID();
            Integer stepId = 4;

            when(stepExecutionStatusRepository
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    StepExecutionStatusNotFoundException.class,
                    () -> stepExecutionService.pause(workflowExecutionId, stepId)
            );

            verify(stepExecutionStatusRepository)
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId);
        }
    }

    @Nested
    @DisplayName("resume()")
    class ResumeTests {

        @Test
        @DisplayName("should call resume() on found step")
        void resumeCallsEntityResumeWhenStepFound() {
            UUID workflowExecutionId = UUID.randomUUID();
            Integer stepId = 5;
            StepExecutionStatusEntity stepEntity = mock(StepExecutionStatusEntity.class);

            when(stepExecutionStatusRepository
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId))
                    .thenReturn(Optional.of(stepEntity));

            stepExecutionService.resume(workflowExecutionId, stepId);

            verify(stepExecutionStatusRepository)
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId);
            verify(stepEntity).resume();
            verifyNoMoreInteractions(stepEntity);
        }

        @Test
        @DisplayName("should throw StepExecutionStatusNotFoundException when step not found")
        void resumeThrowsWhenStepNotFound() {
            UUID workflowExecutionId = UUID.randomUUID();
            Integer stepId = 5;

            when(stepExecutionStatusRepository
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    StepExecutionStatusNotFoundException.class,
                    () -> stepExecutionService.resume(workflowExecutionId, stepId)
            );

            verify(stepExecutionStatusRepository)
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(workflowExecutionId, stepId);
        }
    }

    @Nested
    @DisplayName("fail()")
    class FailTests {

        @Test
        @DisplayName("marks step as failed and ready steps as skipped")
        void failCallsEntityFailWhenStepFound() {
            UUID workflowExecutionId = UUID.randomUUID();
            Integer stepId = 5;

            StepExecutionStatusEntity failedStep = mock(StepExecutionStatusEntity.class);
            StepExecutionStatusEntity readyStep = mock(StepExecutionStatusEntity.class);
            StepExecutionStatusEntity completedStep = mock(StepExecutionStatusEntity.class);

            when(failedStep.getStepId()).thenReturn(5);
            when(readyStep.getStepId()).thenReturn(6);
            when(completedStep.getStepId()).thenReturn(7);

            when(readyStep.getStatus()).thenReturn(JobStatus.READY);
            when(completedStep.getStatus()).thenReturn(JobStatus.COMPLETED);

            when(stepExecutionStatusRepository
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdOrderByStepIdAsc(workflowExecutionId))
                    .thenReturn(List.of(failedStep, readyStep, completedStep));

            stepExecutionService.fail(workflowExecutionId, stepId);

            verify(failedStep).fail();
            verify(readyStep).markSkipped();

            verify(completedStep, never()).markSkipped();
            verify(completedStep, never()).fail();

            verify(stepExecutionStatusRepository)
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdOrderByStepIdAsc(workflowExecutionId);
        }

        @Test
        @DisplayName("throws exception if stepId to fail is not found")
        void failThrowsWhenStepNotFound() {
            UUID workflowExecutionId = UUID.randomUUID();

            StepExecutionStatusEntity step = mock(StepExecutionStatusEntity.class);

            when(step.getStepId()).thenReturn(10);

            when(stepExecutionStatusRepository
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdOrderByStepIdAsc(workflowExecutionId))
                    .thenReturn(List.of(step));

            assertThrows(
                    StepExecutionStatusNotFoundException.class,
                    () -> stepExecutionService.fail(workflowExecutionId, 5)
            );

            verify(step, never()).fail();
        }
    }

    @Nested
    @DisplayName("terminateAll()")
    class TerminateAllTests {

        @Test
        @DisplayName("marks running and paused steps terminated and ready steps skipped")
        void terminatesRunningPausedAndSkipsReadySteps() {
            UUID workflowExecutionId = UUID.randomUUID();

            StepExecutionStatusEntity runningStep = mock(StepExecutionStatusEntity.class);
            StepExecutionStatusEntity pausedStep = mock(StepExecutionStatusEntity.class);
            StepExecutionStatusEntity readyStep = mock(StepExecutionStatusEntity.class);
            StepExecutionStatusEntity completedStep = mock(StepExecutionStatusEntity.class);

            when(runningStep.getStatus()).thenReturn(JobStatus.RUNNING);
            when(pausedStep.getStatus()).thenReturn(JobStatus.PAUSED);
            when(readyStep.getStatus()).thenReturn(JobStatus.READY);
            when(completedStep.getStatus()).thenReturn(JobStatus.COMPLETED);

            when(stepExecutionStatusRepository
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdOrderByStepIdAsc(workflowExecutionId))
                    .thenReturn(List.of(
                            runningStep,
                            pausedStep,
                            readyStep,
                            completedStep
                    ));

            stepExecutionService.terminateAll(workflowExecutionId);

            verify(runningStep).markTerminated();
            verify(pausedStep).markTerminated();
            verify(readyStep).markSkipped();

            verify(completedStep, never()).markTerminated();
            verify(completedStep, never()).markSkipped();

            verify(stepExecutionStatusRepository)
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdOrderByStepIdAsc(workflowExecutionId);
        }

        @Test
        @DisplayName("does nothing when there are no steps for the execution")
        void doesNothingWhenNoStepsFound() {
            UUID workflowExecutionId = UUID.randomUUID();

            when(stepExecutionStatusRepository
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdOrderByStepIdAsc(workflowExecutionId))
                    .thenReturn(List.of());

            stepExecutionService.terminateAll(workflowExecutionId);

            verify(stepExecutionStatusRepository)
                    .findByWorkflowExecutionEntity_WorkflowExecutionIdOrderByStepIdAsc(workflowExecutionId);
        }

    }

}