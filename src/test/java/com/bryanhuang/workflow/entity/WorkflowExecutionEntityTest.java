package com.bryanhuang.workflow.entity;

import com.bryanhuang.workflow.model.JobStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowExecutionEntityTest {

    private WorkflowExecutionEntity createEntityWithStatus(JobStatus status) {
        return WorkflowExecutionEntity.builder()
                .workflowExecutionId(UUID.randomUUID())
                .workflowId(UUID.randomUUID())
                .status(status)
                .createdAt(null)
                .startedAt(null)
                .completedAt(null)
                .build();
    }

    @Nested
    @DisplayName("start()")
    class StartTests {

        @Test
        @DisplayName("start() should transition from READY to RUNNING and set startedAt")
        void startFromReady() {
            WorkflowExecutionEntity entity = createEntityWithStatus(JobStatus.READY);

            entity.start();

            assertEquals(JobStatus.RUNNING, entity.getStatus());
            assertNotNull(entity.getStartedAt(), "startedAt should be set when starting");
            // completedAt should remain null
            assertNull(entity.getCompletedAt());
        }

        @Test
        @DisplayName("start() should fail when status is not READY")
        void startFromNonReadyShouldThrow() {
            for (JobStatus status : JobStatus.values()) {
                if (status == JobStatus.READY) continue;

                WorkflowExecutionEntity entity = createEntityWithStatus(status);
                IllegalStateException ex = assertThrows(
                        IllegalStateException.class,
                        entity::start,
                        "Expected start() to throw when status is " + status
                );
                assertNull(entity.getCompletedAt());
                assertEquals("Execution must be READY to start.", ex.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("fail()")
    class FailTests {

        @Test
        @DisplayName("fail() should transition from RUNNING to FAILED and set completedAt")
        void failFromRunning() {
            WorkflowExecutionEntity entity = createEntityWithStatus(JobStatus.RUNNING);

            entity.fail();

            assertEquals(JobStatus.FAILED, entity.getStatus());
            assertNotNull(entity.getCompletedAt(), "completedAt should be set when failing");
        }

        @Test
        @DisplayName("fail() should fail when status is not RUNNING")
        void failFromNonRunningShouldThrow() {
            for (JobStatus status : JobStatus.values()) {
                if (status == JobStatus.RUNNING) continue;

                WorkflowExecutionEntity entity = createEntityWithStatus(status);
                IllegalStateException ex = assertThrows(
                        IllegalStateException.class,
                        entity::fail,
                        "Expected fail() to throw when status is " + status
                );
                assertEquals("Execution must be RUNNING to fail.", ex.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("complete()")
    class CompleteTests {

        @Test
        @DisplayName("complete() should transition from RUNNING to COMPLETED and set completedAt")
        void completeFromRunning() {
            WorkflowExecutionEntity entity = createEntityWithStatus(JobStatus.RUNNING);

            entity.complete();

            assertEquals(JobStatus.COMPLETED, entity.getStatus());
            assertNotNull(entity.getCompletedAt(), "completedAt should be set when completing");
        }

        @Test
        @DisplayName("complete() should fail when status is not RUNNING")
        void completeFromNonRunningShouldThrow() {
            for (JobStatus status : JobStatus.values()) {
                if (status == JobStatus.RUNNING) continue;

                WorkflowExecutionEntity entity = createEntityWithStatus(status);
                IllegalStateException ex = assertThrows(
                        IllegalStateException.class,
                        entity::complete,
                        "Expected complete() to throw when status is " + status
                );
                assertEquals("Execution must be RUNNING to complete.", ex.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("terminate()")
    class TerminateTests {

        @Test
        @DisplayName("terminate() should transition from RUNNING to TERMINATED and set completedAt")
        void terminateFromRunning() {
            WorkflowExecutionEntity entity = createEntityWithStatus(JobStatus.RUNNING);

            entity.terminate();

            assertEquals(JobStatus.TERMINATED, entity.getStatus());
            assertNotNull(entity.getCompletedAt(), "completedAt should be set when terminating");
        }

        @Test
        @DisplayName("terminate() should transition from PAUSED to TERMINATED and set completedAt")
        void terminateFromPaused() {
            WorkflowExecutionEntity entity = createEntityWithStatus(JobStatus.PAUSED);

            entity.terminate();

            assertEquals(JobStatus.TERMINATED, entity.getStatus());
            assertNotNull(entity.getCompletedAt(), "completedAt should be set when terminating");
        }

        @Test
        @DisplayName("terminate() should fail when status is not RUNNING")
        void terminateFromNonRunningShouldThrow() {
            for (JobStatus status : JobStatus.values()) {
                if (status == JobStatus.RUNNING || status == JobStatus.PAUSED) continue;

                WorkflowExecutionEntity entity = createEntityWithStatus(status);
                IllegalStateException ex = assertThrows(
                        IllegalStateException.class,
                        entity::terminate,
                        "Expected terminate() to throw when status is " + status
                );
                assertEquals("Execution must be RUNNING or PAUSED to terminate.", ex.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("isTerminal()")
    class IsTerminalTests {

        @Test
        @DisplayName("isTerminal() should be false for READY and RUNNING")
        void nonTerminalStatuses() {
            WorkflowExecutionEntity ready = createEntityWithStatus(JobStatus.READY);
            WorkflowExecutionEntity running = createEntityWithStatus(JobStatus.RUNNING);

            assertFalse(ready.isTerminal(), "READY should not be terminal");
            assertFalse(running.isTerminal(), "RUNNING should not be terminal");
        }

        @Test
        @DisplayName("isTerminal() should be true for COMPLETED, FAILED and TERMINATED")
        void terminalStatuses() {
            WorkflowExecutionEntity completed = createEntityWithStatus(JobStatus.COMPLETED);
            WorkflowExecutionEntity failed = createEntityWithStatus(JobStatus.FAILED);
            WorkflowExecutionEntity terminated = createEntityWithStatus(JobStatus.TERMINATED);

            assertTrue(completed.isTerminal(), "COMPLETED should be terminal");
            assertTrue(failed.isTerminal(), "FAILED should be terminal");
            assertTrue(terminated.isTerminal(), "TERMINATED should be terminal");
        }
    }

    @Test
    @DisplayName("Builder and constructor should set fields correctly")
    void builderAndConstructor() {
        UUID id = UUID.randomUUID();
        int stepId = 5;
        List<Integer> dependsOn = List.of(1, 2);
        Instant started = Instant.now().minusSeconds(10);
        Instant completed = Instant.now();

        StepExecutionStatusEntity entity = StepExecutionStatusEntity.builder()
                .id(id)
                .workflowExecutionEntity(null)
                .stepId(stepId)
                .stepName(null)
                .status(JobStatus.READY)
                .startedAt(started)
                .completedAt(completed)
                .dependsOnStepIds(dependsOn)
                .build();

        assertEquals(id, entity.getId());
        assertEquals(stepId, entity.getStepId());
        assertEquals(JobStatus.READY, entity.getStatus());
        assertEquals(started, entity.getStartedAt());
        assertEquals(completed, entity.getCompletedAt());
        assertEquals(dependsOn, entity.getDependsOnStepIds());
    }

    @Nested
    @DisplayName("pause()")
    class PauseTests {

        @Test
        @DisplayName("pause() should transition from RUNNING to PAUSED")
        void pauseFromRunning() {
            WorkflowExecutionEntity entity = createEntityWithStatus(JobStatus.RUNNING);
            entity.pause();

            assertEquals(JobStatus.PAUSED, entity.getStatus());
            assertNull(entity.getCompletedAt());

        }

        @Test
        @DisplayName("pause() should fail when status is not RUNNING")
        void pauseFromNonRunningShouldThrow() {
            for (JobStatus status : JobStatus.values()) {
                if (status == JobStatus.RUNNING) continue;

                WorkflowExecutionEntity entity = createEntityWithStatus(status);
                IllegalStateException ex = assertThrows(
                        IllegalStateException.class,
                        entity::pause,
                        "Expected pause() to throw when status is " + status
                );
                assertEquals("Execution must be RUNNING to pause.", ex.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("resume()")
    class ResumeTests {

        @Test
        @DisplayName("resume() should transition from PAUSED to RUNNING")
        void resumeFromPaused() {
            WorkflowExecutionEntity entity = createEntityWithStatus(JobStatus.PAUSED);
            entity.resume();
            assertEquals(JobStatus.RUNNING, entity.getStatus());
            assertNull(entity.getCompletedAt());
        }

        @Test
        @DisplayName("resume() should fail when status is not PAUSED")
        void resumeFromNonPausedShouldThrow() {
            for (JobStatus status : JobStatus.values()) {
                if (status == JobStatus.PAUSED) continue;
                WorkflowExecutionEntity entity = createEntityWithStatus(status);
                IllegalStateException ex = assertThrows(
                        IllegalStateException.class,
                        entity::resume,
                        "Expected resume() to throw when status is " + status
                );
                assertEquals("Execution must be PAUSED to resume.", ex.getMessage());
            }
        }

    }

}
