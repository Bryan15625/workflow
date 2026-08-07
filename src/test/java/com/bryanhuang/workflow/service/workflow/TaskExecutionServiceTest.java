package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import com.bryanhuang.workflow.exception.InvalidWorkflowException;
import com.bryanhuang.workflow.model.workflow.JobControl;
import com.bryanhuang.workflow.model.workflow.Step;
import com.bryanhuang.workflow.model.workflow.Workflow;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private StepExecutorService stepExecutorService;

    @Mock
    private WorkflowControlGate workflowControlGate;

    @Mock
    private WorkflowCleanupService workflowCleanupService;

    @InjectMocks
    private TaskExecutionService taskExecutionService;

    // Helper to build a mocked Step with a stepId and no dependencies,
    // so buildExecutionOrder resolves it without null pointer exception on getDependsOnStepIds().
    private Step mockStep(Integer stepId) {
        Step step = mock(Step.class);
        when(step.getStepId()).thenReturn(stepId);
        when(step.getDependsOnStepIds()).thenReturn(List.of());
        return step;
    }

    @Nested
    @DisplayName("executeWorkflow()")
    class ExecuteWorkflowTests {

        @Test
        @DisplayName("happy path: gate returns NONE for every step, all steps execute, workflow completes")
        void executesAllStepsAndCompletes_whenGateNeverBlocks() throws Exception {
            UUID workflowExecutionId = UUID.randomUUID();
            UUID workflowId = UUID.randomUUID();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
            when(entity.getWorkflowId()).thenReturn(workflowId);

            Step step1 = mockStep(1);
            Step step2 = mockStep(2);

            Workflow workflow = mock(Workflow.class);
            when(workflow.getSteps()).thenReturn(List.of(step1, step2));
            when(workflowQueryService.findWorkflowEntityAndMapToWorkflow(workflowId))
                    .thenReturn(workflow);

            when(workflowControlGate.checkpoint(workflowExecutionId))
                    .thenReturn(JobControl.NONE);

            taskExecutionService.executeWorkflow(entity);

            InOrder inOrder = inOrder(
                    workflowExecutionService,
                    workflowQueryService,
                    stepExecutionService,
                    workflowControlGate,
                    stepExecutorService
            );

            inOrder.verify(workflowExecutionService).start(workflowExecutionId);
            inOrder.verify(workflowQueryService).findWorkflowEntityAndMapToWorkflow(workflowId);

            inOrder.verify(workflowControlGate).checkpoint(workflowExecutionId);
            inOrder.verify(stepExecutionService).start(workflowExecutionId, 1);
            inOrder.verify(stepExecutorService).execute(eq(step1), any(), any());
            inOrder.verify(stepExecutionService).complete(workflowExecutionId, 1);

            inOrder.verify(workflowControlGate).checkpoint(workflowExecutionId);
            inOrder.verify(stepExecutionService).start(workflowExecutionId, 2);
            inOrder.verify(stepExecutorService).execute(eq(step2), any(), any());
            inOrder.verify(stepExecutionService).complete(workflowExecutionId, 2);

            inOrder.verify(workflowExecutionService).complete(workflowExecutionId);
            verify(workflowExecutionService, never()).fail(any(), any());
        }

        @Test
        @DisplayName("terminates immediately when gate returns TERMINATE before first step")
        void terminatesImmediately_whenGateReturnsTerminateBeforeFirstStep() throws InterruptedException {
            UUID workflowExecutionId = UUID.randomUUID();
            UUID workflowId = UUID.randomUUID();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
            when(entity.getWorkflowId()).thenReturn(workflowId);

            Step step1 = mockStep(1);

            Workflow workflow = mock(Workflow.class);
            when(workflow.getSteps()).thenReturn(List.of(step1));
            when(workflowQueryService.findWorkflowEntityAndMapToWorkflow(workflowId))
                    .thenReturn(workflow);

            when(workflowControlGate.checkpoint(workflowExecutionId))
                    .thenReturn(JobControl.TERMINATE);

            taskExecutionService.executeWorkflow(entity);

            verify(workflowControlGate).checkpoint(workflowExecutionId);
            verify(stepExecutionService, never()).start(any(), any());
            verify(stepExecutionService, never()).complete(any(), any());
            verify(stepExecutorService, never()).execute(eq(step1), any(), any());

            verify(workflowExecutionService, never()).complete(workflowExecutionId);
            verify(workflowExecutionService, never()).fail(eq(workflowExecutionId), any());
            verify(workflowCleanupService, never()).cleanupWorkflowExecution(workflowExecutionId);
        }

        @Test
        @DisplayName("terminates between steps: first step executes, gate blocks before second step")
        void terminatesBetweenSteps_whenGateReturnsTerminateOnSecondCheckpoint() throws Exception {
            UUID workflowExecutionId = UUID.randomUUID();
            UUID workflowId = UUID.randomUUID();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
            when(entity.getWorkflowId()).thenReturn(workflowId);

            Step step1 = mockStep(1);
            Step step2 = mockStep(2);

            Workflow workflow = mock(Workflow.class);
            when(workflow.getSteps()).thenReturn(List.of(step1, step2));
            when(workflowQueryService.findWorkflowEntityAndMapToWorkflow(workflowId))
                    .thenReturn(workflow);

            // First checkpoint call (before step1) returns NONE, second (before step2) returns TERMINATE
            when(workflowControlGate.checkpoint(workflowExecutionId))
                    .thenReturn(JobControl.NONE, JobControl.TERMINATE);

            taskExecutionService.executeWorkflow(entity);

            verify(stepExecutionService).start(workflowExecutionId, 1);
            verify(stepExecutorService).execute(eq(step1), any(), any());
            verify(stepExecutionService).complete(workflowExecutionId, 1);

            verify(stepExecutionService, never()).start(workflowExecutionId, 2);
            verify(stepExecutorService, never()).execute(eq(step2), any(), any());
            verify(stepExecutionService, never()).complete(workflowExecutionId, 2);

            verify(workflowExecutionService, never()).complete(workflowExecutionId);
            verify(workflowExecutionService, never()).fail(eq(workflowExecutionId), any());
            verify(workflowCleanupService, never()).cleanupWorkflowExecution(workflowExecutionId);
        }

        @Test
        @DisplayName("marks workflow as failed when a step throws, and does not complete")
        void marksWorkflowFailed_whenStepExecutionThrows() throws InterruptedException {
            UUID workflowExecutionId = UUID.randomUUID();
            UUID workflowId = UUID.randomUUID();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
            when(entity.getWorkflowId()).thenReturn(workflowId);

            Step step1 = mockStep(1);

            Workflow workflow = mock(Workflow.class);
            when(workflow.getSteps()).thenReturn(List.of(step1));
            when(workflowQueryService.findWorkflowEntityAndMapToWorkflow(workflowId))
                    .thenReturn(workflow);

            when(workflowControlGate.checkpoint(workflowExecutionId))
                    .thenReturn(JobControl.NONE);

            RuntimeException stepError = new RuntimeException("step failure");
            doThrow(stepError)
                    .when(stepExecutionService)
                    .start(workflowExecutionId, 1);

            taskExecutionService.executeWorkflow(entity);

            verify(workflowExecutionService).start(workflowExecutionId);
            verify(stepExecutorService, never()).execute(eq(step1), any(), any());
            verify(stepExecutionService, never()).complete(workflowExecutionId, 1);

            verify(stepExecutionService).fail(workflowExecutionId, 1);
            verify(workflowExecutionService).fail(eq(workflowExecutionId), any());
            verify(workflowCleanupService).cleanupWorkflowExecution(workflowExecutionId);
            verify(workflowExecutionService, never()).complete(workflowExecutionId);
        }

        @Test
        @DisplayName("ignores IllegalStateException when marking workflow as failed")
        void ignoresIllegalStateExceptionFromFail() throws InterruptedException {
            UUID workflowExecutionId = UUID.randomUUID();
            UUID workflowId = UUID.randomUUID();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
            when(entity.getWorkflowId()).thenReturn(workflowId);

            Step step1 = mockStep(1);

            Workflow workflow = mock(Workflow.class);
            when(workflow.getSteps()).thenReturn(List.of(step1));
            when(workflowQueryService.findWorkflowEntityAndMapToWorkflow(workflowId))
                    .thenReturn(workflow);

            when(workflowControlGate.checkpoint(workflowExecutionId))
                    .thenReturn(JobControl.NONE);

            doThrow(new RuntimeException("step failure"))
                    .when(stepExecutionService)
                    .start(workflowExecutionId, 1);

            doThrow(new IllegalStateException("already completed"))
                    .when(workflowExecutionService)
                    .fail(eq(workflowExecutionId), anyString());

            taskExecutionService.executeWorkflow(entity);

            verify(stepExecutionService).start(workflowExecutionId, 1);
            verify(stepExecutionService).fail(workflowExecutionId, 1);
            verify(workflowExecutionService).start(workflowExecutionId);
            verify(workflowExecutionService).fail(eq(workflowExecutionId), anyString());
            verify(workflowExecutionService, never()).complete(workflowExecutionId);
        }
    }

    @Nested
    @DisplayName("buildExecutionOrder()")
    class BuildExecutionOrderTests {

        @Test
        @DisplayName("builds execution order from steps correctly")
        void buildExecutionOrder_buildsFromSteps() {
            Step step1 = Step.builder().stepId(1).dependsOnStepIds(List.of()).build();
            Step step2 = Step.builder().stepId(2).dependsOnStepIds(List.of(4)).build();
            Step step3 = Step.builder().stepId(3).dependsOnStepIds(List.of(2)).build();
            Step step4 = Step.builder().stepId(4).dependsOnStepIds(List.of(5)).build();
            Step step5 = Step.builder().stepId(5).dependsOnStepIds(List.of(1)).build();
            List<Step> steps = List.of(step1, step2, step3, step4, step5);

            List<Step> executionOrder = taskExecutionService.buildExecutionOrder(steps);

            List<Step> expectedOrder = List.of(step1, step5, step4, step2, step3);
            assertEquals(executionOrder, expectedOrder);
        }

        @Test
        @DisplayName("execution order cannot be computed due to cycle")
        void buildExecutionOrder_cannotBuildFromSteps() {
            Step step1 = Step.builder().stepId(1).dependsOnStepIds(List.of()).build();
            Step step2 = Step.builder().stepId(2).dependsOnStepIds(List.of(4)).build();
            Step step3 = Step.builder().stepId(3).dependsOnStepIds(List.of(2)).build();
            Step step4 = Step.builder().stepId(4).dependsOnStepIds(List.of(2)).build();
            Step step5 = Step.builder().stepId(5).dependsOnStepIds(List.of(1)).build();
            List<Step> steps = List.of(step1, step2, step3, step4, step5);

            InvalidWorkflowException ex = assertThrows(
                    InvalidWorkflowException.class,
                    () -> taskExecutionService.buildExecutionOrder(steps)
            );
            assertEquals("No executable step found. Dependency graph is invalid.", ex.getMessage());
        }
    }
}