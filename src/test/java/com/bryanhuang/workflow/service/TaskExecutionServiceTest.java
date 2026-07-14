package com.bryanhuang.workflow.service;

import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import com.bryanhuang.workflow.model.JobControl;
import com.bryanhuang.workflow.model.Step;
import com.bryanhuang.workflow.model.Workflow;
import com.bryanhuang.workflow.redis.service.WorkflowExecutionRedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskExecutionServiceTest {

    @Mock
    private WorkflowQueryService workflowQueryService;

    @Mock
    private StepExecutionService stepExecutionService;

    @Mock
    private WorkflowExecutionService workflowExecutionService;

    @Mock
    private WorkflowExecutionRedisService workflowExecutionRedisService;

    @InjectMocks
    private TaskExecutionService taskExecutionService;

    @Nested
    @DisplayName("executeWorkflow()")
    class ExecuteWorkflowTests {

        @Test
        @DisplayName("happy path: no control signals, all steps succeed, workflow completes")
        void executesAllStepsAndCompletes_whenNoControlAndNoErrors() throws Exception {
            UUID workflowExecutionId = UUID.randomUUID();
            UUID workflowId = UUID.randomUUID();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
            when(entity.getWorkflowId()).thenReturn(workflowId);

            Step step1 = mock(Step.class);
            Step step2 = mock(Step.class);
            when(step1.getStepId()).thenReturn(1);
            when(step2.getStepId()).thenReturn(2);

            Workflow workflow = mock(Workflow.class);
            when(workflow.getSteps()).thenReturn(List.of(step1, step2));
            when(workflowQueryService.findWorkflowEntityAndMapToWorkflow(workflowId))
                    .thenReturn(workflow);

            // No control signal for the whole execution
            when(workflowExecutionRedisService.getControl(workflowExecutionId))
                    .thenReturn(null);

            // Act
            taskExecutionService.executeWorkflow(entity);

            // Assert
            InOrder inOrder = inOrder(
                    workflowExecutionService,
                    workflowQueryService,
                    stepExecutionService
            );

            // start -> load workflow -> step1 -> step2 -> complete
            inOrder.verify(workflowExecutionService).start(workflowExecutionId);
            inOrder.verify(workflowQueryService).findWorkflowEntityAndMapToWorkflow(workflowId);

            inOrder.verify(stepExecutionService).start(workflowExecutionId, 1);
            inOrder.verify(stepExecutionService).complete(workflowExecutionId, 1);

            inOrder.verify(stepExecutionService).start(workflowExecutionId, 2);
            inOrder.verify(stepExecutionService).complete(workflowExecutionId, 2);

            inOrder.verify(workflowExecutionService).complete(workflowExecutionId);

            // No fail() in happy path
            verify(workflowExecutionService, never()).fail(any());
        }

        @Test
        @DisplayName("happy path: resume signal sent while running, continue running")
        void executesAllStepsAndCompletes_whenOnlyResumeControlAndNoErrors() throws Exception {
            UUID workflowExecutionId = UUID.randomUUID();
            UUID workflowId = UUID.randomUUID();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
            when(entity.getWorkflowId()).thenReturn(workflowId);

            Step step1 = mock(Step.class);
            Step step2 = mock(Step.class);
            when(step1.getStepId()).thenReturn(1);
            when(step2.getStepId()).thenReturn(2);

            Workflow workflow = mock(Workflow.class);
            when(workflow.getSteps()).thenReturn(List.of(step1, step2));
            when(workflowQueryService.findWorkflowEntityAndMapToWorkflow(workflowId))
                    .thenReturn(workflow);

            // No control signal for the whole execution
            when(workflowExecutionRedisService.getControl(workflowExecutionId))
                    .thenReturn(JobControl.RESUME);

            // Act
            taskExecutionService.executeWorkflow(entity);

            // Assert
            InOrder inOrder = inOrder(
                    workflowExecutionService,
                    workflowQueryService,
                    stepExecutionService
            );

            // start -> load workflow -> step1 -> step2 -> complete
            inOrder.verify(workflowExecutionService).start(workflowExecutionId);
            inOrder.verify(workflowQueryService).findWorkflowEntityAndMapToWorkflow(workflowId);

            inOrder.verify(stepExecutionService).start(workflowExecutionId, 1);
            inOrder.verify(stepExecutionService).complete(workflowExecutionId, 1);

            inOrder.verify(stepExecutionService).start(workflowExecutionId, 2);
            inOrder.verify(stepExecutionService).complete(workflowExecutionId, 2);

            inOrder.verify(workflowExecutionService).complete(workflowExecutionId);

            // No fail() in happy path
            verify(workflowExecutionService, never()).fail(any());
        }

        @Test
        @DisplayName("terminates early when control is TERMINATE before first step")
        void terminatesImmediately_whenControlIsTerminateBeforeSteps() {
            UUID workflowExecutionId = UUID.randomUUID();
            UUID workflowId = UUID.randomUUID();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
            when(entity.getWorkflowId()).thenReturn(workflowId);

            // Workflow with some steps (they should never be executed)
            Step step = mock(Step.class);
            Workflow workflow = mock(Workflow.class);
            when(workflow.getSteps()).thenReturn(List.of(step));
            when(workflowQueryService.findWorkflowEntityAndMapToWorkflow(workflowId))
                    .thenReturn(workflow);

            // First control check returns TERMINATE
            when(workflowExecutionRedisService.getControl(workflowExecutionId))
                    .thenReturn(JobControl.TERMINATE);

            // Act
            taskExecutionService.executeWorkflow(entity);

            // Assert
            InOrder inOrder = inOrder(
                    workflowExecutionService,
                    workflowQueryService
            );

            inOrder.verify(workflowExecutionService).start(workflowExecutionId);
            inOrder.verify(workflowQueryService).findWorkflowEntityAndMapToWorkflow(workflowId);
            inOrder.verify(workflowExecutionService).terminate(workflowExecutionId);

            // No step execution and no completion
            verify(stepExecutionService, never()).start(any(), any());
            verify(stepExecutionService, never()).complete(any(), any());
            verify(workflowExecutionService, never()).complete(workflowExecutionId);
            verify(workflowExecutionService, never()).fail(workflowExecutionId);
        }

        @Test
        @DisplayName("job is running then pause control is received, then resume control is received")
        void pausesThenResumesAndContinues() throws Exception {
            UUID workflowExecutionId = UUID.randomUUID();
            UUID workflowId = UUID.randomUUID();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
            when(entity.getWorkflowId()).thenReturn(workflowId);

            // Single step, we want to see that it still runs after PAUSE to RESUME
            Step step1 = mock(Step.class);
            when(step1.getStepId()).thenReturn(1);

            Workflow workflow = mock(Workflow.class);
            when(workflow.getSteps()).thenReturn(List.of(step1));
            when(workflowQueryService.findWorkflowEntityAndMapToWorkflow(workflowId))
                    .thenReturn(workflow);

            // Control sequence:
            // 1) First handleWorkflowControl call (before step): PAUSE
            // 2) Inside PAUSE loop: null (ignored, continue)
            // 3) Inside PAUSE loop: RESUME (resume + return)
            when(workflowExecutionRedisService.getControl(workflowExecutionId))
                    .thenReturn(
                            JobControl.PAUSE,   // initial call
                            null,               // first iteration in while
                            JobControl.RESUME   // second iteration in while
                    );

            // Act
            taskExecutionService.executeWorkflow(entity);

            // Assert: execution is paused, then resumed, then step runs and workflow completes
            InOrder inOrder = inOrder(
                    workflowExecutionService,
                    workflowQueryService,
                    stepExecutionService
            );

            // Start and load workflow
            inOrder.verify(workflowExecutionService).start(workflowExecutionId);
            inOrder.verify(workflowQueryService).findWorkflowEntityAndMapToWorkflow(workflowId);

            // handleWorkflowControl: first call -> PAUSE
            inOrder.verify(workflowExecutionService).pause(workflowExecutionId);

            // inside while: null -> continue
            // inside while: RESUME -> resume + return
            inOrder.verify(workflowExecutionService).resume(workflowExecutionId);

            // After RESUME, step should execute and workflow should complete
            inOrder.verify(stepExecutionService).start(workflowExecutionId, 1);
            inOrder.verify(stepExecutionService).complete(workflowExecutionId, 1);
            inOrder.verify(workflowExecutionService).complete(workflowExecutionId);

            verify(workflowExecutionService, never()).fail(any());
            verify(workflowExecutionService, never()).terminate(workflowExecutionId);
        }

        @Test
        @DisplayName("job is running then pause and several other controls are received, finally terminate control is received")
        void pausesThenResumesAndTerminates() throws Exception {
            UUID workflowExecutionId = UUID.randomUUID();
            UUID workflowId = UUID.randomUUID();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
            when(entity.getWorkflowId()).thenReturn(workflowId);

            // Single step, we want to see that it still runs after PAUSE to RESUME
            Step step1 = mock(Step.class);

            Workflow workflow = mock(Workflow.class);
            when(workflow.getSteps()).thenReturn(List.of(step1));
            when(workflowQueryService.findWorkflowEntityAndMapToWorkflow(workflowId))
                    .thenReturn(workflow);

            when(workflowExecutionRedisService.getControl(workflowExecutionId))
                    .thenReturn(
                            JobControl.PAUSE,
                            JobControl.PAUSE,
                            JobControl.NONE,
                            null,
                            JobControl.TERMINATE
                    );

            // Act
            taskExecutionService.executeWorkflow(entity);

            // Assert: execution is paused, then resumed, then step runs and workflow completes
            InOrder inOrder = inOrder(
                    workflowExecutionService,
                    workflowQueryService,
                    stepExecutionService
            );

            // Start and load workflow
            inOrder.verify(workflowExecutionService).start(workflowExecutionId);
            inOrder.verify(workflowQueryService).findWorkflowEntityAndMapToWorkflow(workflowId);

            // handleWorkflowControl: first call -> PAUSE
            inOrder.verify(workflowExecutionService).pause(workflowExecutionId);

            // pause, do nothing
            // none, do nothing
            // null, continue
            // terminate -> return
            inOrder.verify(workflowExecutionService).terminate(workflowExecutionId);

            // After terminate, step should execute and workflow should complete
            verify(stepExecutionService, never()).start(any(), any());
            verify(stepExecutionService, never()).complete(any(), any());
            verify(workflowExecutionService, never()).fail(any());
            verify(workflowExecutionService, never()).complete(workflowExecutionId);
        }

        @Test
        @DisplayName("marks workflow as failed when a step throws, and propagates exception")
        void marksWorkflowFailed_whenStepExecutionThrows() throws Exception {
            UUID workflowExecutionId = UUID.randomUUID();
            UUID workflowId = UUID.randomUUID();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
            when(entity.getWorkflowId()).thenReturn(workflowId);

            Step step1 = mock(Step.class);
            when(step1.getStepId()).thenReturn(1);

            Workflow workflow = mock(Workflow.class);
            when(workflow.getSteps()).thenReturn(List.of(step1));
            when(workflowQueryService.findWorkflowEntityAndMapToWorkflow(workflowId))
                    .thenReturn(workflow);

            when(workflowExecutionRedisService.getControl(workflowExecutionId))
                    .thenReturn(null);

            // Cause stepExecutionService.start to throw -> executeStep's catch will fail() and rethrow
            RuntimeException stepError = new RuntimeException("step failure");
            doThrow(stepError)
                    .when(stepExecutionService)
                    .start(workflowExecutionId, 1);

            // Act
            taskExecutionService.executeWorkflow(entity);

            // Assert: workflowExecutionService.fail must be called (outer catch)
            verify(workflowExecutionService).start(workflowExecutionId);
            verify(workflowExecutionService).fail(workflowExecutionId);

            verify(stepExecutionService).fail(workflowExecutionId, 1);

            // complete() should not be called
            verify(workflowExecutionService, never()).complete(workflowExecutionId);
        }

        @Test
        @DisplayName("ignores IllegalStateException when marking workflow as failed")
        void ignoresIllegalStateExceptionFromFail() throws Exception {
            UUID workflowExecutionId = UUID.randomUUID();
            UUID workflowId = UUID.randomUUID();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
            when(entity.getWorkflowId()).thenReturn(workflowId);

            Step step1 = mock(Step.class);
            when(step1.getStepId()).thenReturn(1);

            Workflow workflow = mock(Workflow.class);
            when(workflow.getSteps()).thenReturn(List.of(step1));
            when(workflowQueryService.findWorkflowEntityAndMapToWorkflow(workflowId))
                    .thenReturn(workflow);

            when(workflowExecutionRedisService.getControl(workflowExecutionId))
                    .thenReturn(null);

            // Step throws
            doThrow(new RuntimeException("step failure"))
                    .when(stepExecutionService)
                    .start(workflowExecutionId, 1);

            // And fail() itself throws IllegalStateException (already finalized, etc.)
            doThrow(new IllegalStateException("already completed"))
                    .when(workflowExecutionService)
                    .fail(workflowExecutionId);

            // Act: should not rethrow despite fail() throwing
            taskExecutionService.executeWorkflow(entity);

            // Assert
            verify(stepExecutionService).fail(workflowExecutionId, 1);
            verify(workflowExecutionService).start(workflowExecutionId);
            verify(workflowExecutionService).fail(workflowExecutionId);
            verify(workflowExecutionService, never()).complete(workflowExecutionId);
        }
    }

    @Nested
    @DisplayName("terminateWorkflow()")
    class TerminateWorkflowTests {

        @Test
        @DisplayName("delegates to WorkflowExecutionService.terminate")
        void terminateWorkflowDelegatesToService() {
            UUID workflowExecutionId = UUID.randomUUID();

            taskExecutionService.terminateWorkflow(workflowExecutionId);

            verify(workflowExecutionService).terminate(workflowExecutionId);
            verifyNoMoreInteractions(workflowExecutionService);
        }
    }
}