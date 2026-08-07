package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.model.workflow.JobControl;
import com.bryanhuang.workflow.redis.service.WorkflowExecutionRedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowControlGateTest {

    @Mock
    private WorkflowExecutionRedisService workflowExecutionRedisService;

    @Mock
    private WorkflowExecutionService workflowExecutionService;

    @Mock
    private WorkflowCleanupService workflowCleanupService;

    @Mock
    private StepExecutionService stepExecutionService;

    @InjectMocks
    private WorkflowControlGate workflowControlGate;

    @Nested
    @DisplayName("checkpoint(workflowExecutionId) - between-step, no active step running")
    class CheckpointTests {

        @Test
        @DisplayName("returns NONE and touches nothing when control is null")
        void returnsNone_whenControlIsNull() throws InterruptedException {
            UUID workflowExecutionId = UUID.randomUUID();
            when(workflowExecutionRedisService.getControl(workflowExecutionId)).thenReturn(null);

            JobControl result = workflowControlGate.checkpoint(workflowExecutionId);

            assertEquals(JobControl.NONE, result);
            verifyNoInteractions(workflowExecutionService, workflowCleanupService, stepExecutionService);
        }

        @Test
        @DisplayName("returns NONE as-is when control is NONE")
        void returnsNone_whenControlIsNone() throws InterruptedException {
            UUID workflowExecutionId = UUID.randomUUID();
            when(workflowExecutionRedisService.getControl(workflowExecutionId)).thenReturn(JobControl.NONE);

            JobControl result = workflowControlGate.checkpoint(workflowExecutionId);

            assertEquals(JobControl.NONE, result);
            verifyNoInteractions(workflowExecutionService, workflowCleanupService, stepExecutionService);
        }

        @Test
        @DisplayName("pauses at workflow level only (no step-level pause) and resumes when RESUME arrives")
        void pausesThenResumes_withoutTouchingStepState() throws InterruptedException {
            UUID workflowExecutionId = UUID.randomUUID();

            when(workflowExecutionRedisService.getControl(workflowExecutionId))
                    .thenReturn(
                            JobControl.PAUSE,  // initial call
                            null,              // first iteration in wait loop, ignored
                            JobControl.RESUME  // second iteration, resumes
                    );

            JobControl result = workflowControlGate.checkpoint(workflowExecutionId);

            assertEquals(JobControl.RESUME, result);
            verify(workflowExecutionService).pause(workflowExecutionId);
            verify(workflowExecutionService).resume(workflowExecutionId);
            verifyNoInteractions(stepExecutionService);
        }

        @Test
        @DisplayName("terminates workflow and cleans up when TERMINATE arrives while paused")
        void terminatesWhilePaused() throws InterruptedException {
            UUID workflowExecutionId = UUID.randomUUID();

            when(workflowExecutionRedisService.getControl(workflowExecutionId))
                    .thenReturn(JobControl.PAUSE, JobControl.TERMINATE);

            JobControl result = workflowControlGate.checkpoint(workflowExecutionId);

            assertEquals(JobControl.TERMINATE, result);
            verify(workflowExecutionService).pause(workflowExecutionId);
            verify(workflowExecutionService).terminate(workflowExecutionId);
            verify(stepExecutionService).terminateAll(workflowExecutionId);
            verify(workflowCleanupService).cleanupWorkflowExecution(workflowExecutionId);
            verify(workflowExecutionService, never()).resume(any());
        }

        @Test
        @DisplayName("terminates immediately without pausing when control is TERMINATE outright")
        void terminatesImmediately_whenControlIsTerminate() throws InterruptedException {
            UUID workflowExecutionId = UUID.randomUUID();

            when(workflowExecutionRedisService.getControl(workflowExecutionId))
                    .thenReturn(JobControl.TERMINATE);

            JobControl result = workflowControlGate.checkpoint(workflowExecutionId);

            assertEquals(JobControl.TERMINATE, result);
            verify(workflowExecutionService).terminate(workflowExecutionId);
            verify(stepExecutionService).terminateAll(workflowExecutionId);
            verify(workflowCleanupService).cleanupWorkflowExecution(workflowExecutionId);
            verify(workflowExecutionService, never()).pause(any());
            verify(workflowExecutionService, never()).resume(any());
        }

        @Test
        @DisplayName("swallows IllegalStateException from an already-terminal workflow on terminate")
        void ignoresIllegalStateException_whenAlreadyTerminal() throws InterruptedException {
            UUID workflowExecutionId = UUID.randomUUID();

            when(workflowExecutionRedisService.getControl(workflowExecutionId))
                    .thenReturn(JobControl.TERMINATE);
            doThrow(new IllegalStateException("already completed"))
                    .when(workflowExecutionService)
                    .terminate(workflowExecutionId);

            JobControl result = workflowControlGate.checkpoint(workflowExecutionId);

            assertEquals(JobControl.TERMINATE, result);
            // cleanup should still run even though terminate() threw
            verify(workflowExecutionService).terminate(workflowExecutionId);
            verify(stepExecutionService).terminateAll(workflowExecutionId);
            verify(workflowCleanupService).cleanupWorkflowExecution(workflowExecutionId);
        }
    }

    @Nested
    @DisplayName("checkpointStep(workflowExecutionId, stepId) - mid-step, active step")
    class CheckpointStepTests {

        @Test
        @DisplayName("returns NONE and touches nothing when control is null")
        void returnsNone_whenControlIsNull() throws InterruptedException {
            UUID workflowExecutionId = UUID.randomUUID();
            Integer stepId = 1;
            when(workflowExecutionRedisService.getControl(workflowExecutionId)).thenReturn(null);

            JobControl result = workflowControlGate.checkpointStep(workflowExecutionId, stepId);

            assertEquals(JobControl.NONE, result);
            verifyNoInteractions(workflowExecutionService, workflowCleanupService, stepExecutionService);
        }

        @Test
        @DisplayName("returns NONE as-is when control is NONE")
        void returnsNone_whenControlIsNone() throws InterruptedException {
            UUID workflowExecutionId = UUID.randomUUID();
            Integer stepId = 1;
            when(workflowExecutionRedisService.getControl(workflowExecutionId)).thenReturn(JobControl.NONE);

            JobControl result = workflowControlGate.checkpointStep(workflowExecutionId, stepId);

            assertEquals(JobControl.NONE, result);
            verifyNoInteractions(workflowExecutionService, workflowCleanupService, stepExecutionService);
        }

        @Test
        @DisplayName("pauses both workflow and step level, resumes both when RESUME arrives")
        void pausesThenResumes_touchingBothWorkflowAndStepState() throws InterruptedException {
            UUID workflowExecutionId = UUID.randomUUID();
            Integer stepId = 1;

            when(workflowExecutionRedisService.getControl(workflowExecutionId))
                    .thenReturn(JobControl.PAUSE, null, JobControl.RESUME);

            JobControl result = workflowControlGate.checkpointStep(workflowExecutionId, stepId);

            assertEquals(JobControl.RESUME, result);
            verify(workflowExecutionService).pause(workflowExecutionId);
            verify(stepExecutionService).pause(workflowExecutionId, stepId);
            verify(workflowExecutionService).resume(workflowExecutionId);
            verify(stepExecutionService).resume(workflowExecutionId, stepId);
        }

        @Test
        @DisplayName("terminates workflow and cleans up when TERMINATE arrives while paused mid-step")
        void terminatesWhilePaused() throws InterruptedException {
            UUID workflowExecutionId = UUID.randomUUID();
            Integer stepId = 1;

            when(workflowExecutionRedisService.getControl(workflowExecutionId))
                    .thenReturn(JobControl.PAUSE, JobControl.TERMINATE);

            JobControl result = workflowControlGate.checkpointStep(workflowExecutionId, stepId);

            assertEquals(JobControl.TERMINATE, result);
            verify(workflowExecutionService).pause(workflowExecutionId);
            verify(stepExecutionService).pause(workflowExecutionId, stepId);
            verify(workflowExecutionService).terminate(workflowExecutionId);

            verify(stepExecutionService).terminateAll(workflowExecutionId);
            verify(workflowCleanupService).cleanupWorkflowExecution(workflowExecutionId);
            verify(stepExecutionService, never()).resume(any(), any());
        }

        @Test
        @DisplayName("terminates immediately without step-level pause when control is TERMINATE outright")
        void terminatesImmediately_whenControlIsTerminate() throws InterruptedException {
            UUID workflowExecutionId = UUID.randomUUID();
            Integer stepId = 1;

            when(workflowExecutionRedisService.getControl(workflowExecutionId))
                    .thenReturn(JobControl.TERMINATE);

            JobControl result = workflowControlGate.checkpointStep(workflowExecutionId, stepId);

            assertEquals(JobControl.TERMINATE, result);
            verify(workflowExecutionService).terminate(workflowExecutionId);
            verify(stepExecutionService).terminateAll(workflowExecutionId);
            verify(workflowCleanupService).cleanupWorkflowExecution(workflowExecutionId);
            verify(stepExecutionService, never()).pause(any(), any());
            verify(stepExecutionService, never()).resume(any(), any());
        }
    }
}