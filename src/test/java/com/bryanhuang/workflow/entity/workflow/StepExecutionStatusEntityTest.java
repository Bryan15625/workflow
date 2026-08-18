package com.bryanhuang.workflow.entity.workflow;

import com.bryanhuang.workflow.model.workflow.JobStatus;
import com.bryanhuang.workflow.model.workflow.StepName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StepExecutionStatusEntityTest {

    private StepExecutionStatusEntity createEntityWithStatus(JobStatus status) {
        return new StepExecutionStatusEntity(
                UUID.randomUUID(),
                new WorkflowExecutionEntity(),
                1,
                StepName.INGEST_CSV,
                status,
                null,
                null,
                List.of()
        );
    }

    @Nested
    @DisplayName("start()")
    class StartTests {

        @Test
        @DisplayName("start() should transition from READY to RUNNING and set startedAt")
        void startFromReady() {
            StepExecutionStatusEntity entity = createEntityWithStatus(JobStatus.READY);

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

                StepExecutionStatusEntity entity = createEntityWithStatus(status);
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
            StepExecutionStatusEntity entity = createEntityWithStatus(JobStatus.RUNNING);

            entity.fail();

            assertEquals(JobStatus.FAILED, entity.getStatus());
            assertNotNull(entity.getCompletedAt(), "completedAt should be set when failing");
        }

        @Test
        @DisplayName("fail() should fail when status is not RUNNING")
        void failFromNonRunningShouldThrow() {
            for (JobStatus status : JobStatus.values()) {
                if (status == JobStatus.RUNNING) continue;

                StepExecutionStatusEntity entity = createEntityWithStatus(status);
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
            StepExecutionStatusEntity entity = createEntityWithStatus(JobStatus.RUNNING);

            entity.complete();

            assertEquals(JobStatus.COMPLETED, entity.getStatus());
            assertNotNull(entity.getCompletedAt(), "completedAt should be set when completing");
        }

        @Test
        @DisplayName("complete() should fail when status is not RUNNING")
        void completeFromNonRunningShouldThrow() {
            for (JobStatus status : JobStatus.values()) {
                if (status == JobStatus.RUNNING) continue;

                StepExecutionStatusEntity entity = createEntityWithStatus(status);
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
    @DisplayName("markTerminated()")
    class MarkTerminatedTests {

        @Test
        @DisplayName("terminate() should transition from RUNNING or PAUSED to TERMINATED and set completedAt")
        void terminateFromRunning() {
            StepExecutionStatusEntity running = createEntityWithStatus(JobStatus.RUNNING);
            StepExecutionStatusEntity paused = createEntityWithStatus(JobStatus.PAUSED);

            running.markTerminated();
            paused.markTerminated();

            assertEquals(JobStatus.TERMINATED, running.getStatus());
            assertEquals(JobStatus.TERMINATED, paused.getStatus());
            assertNotNull(running.getCompletedAt(), "completedAt should be set when terminating");
            assertNotNull(paused.getCompletedAt(), "completedAt should be set when terminating");
        }

        @Test
        @DisplayName("terminate() should fail when status is not RUNNING or PAUSED")
        void terminateFromNonRunningShouldThrow() {
            for (JobStatus status : JobStatus.values()) {
                if (status == JobStatus.RUNNING || status == JobStatus.PAUSED) continue;

                StepExecutionStatusEntity entity = createEntityWithStatus(status);
                IllegalStateException ex = assertThrows(
                        IllegalStateException.class,
                        entity::markTerminated,
                        "Expected terminate() to throw when status is " + status
                );
                assertEquals("Execution must be READY or RUNNING to be marked terminated.", ex.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("markSKipped()")
    class MarkSkippedTests {

        @Test
        @DisplayName("markSkipped() should transition to SKIPPED when status is READY")
        void markSkippedWhenReady() {
            StepExecutionStatusEntity entity = createEntityWithStatus(JobStatus.READY);

            entity.markSkipped();
            assertEquals(JobStatus.SKIPPED, entity.getStatus());
        }

        @Test
        @DisplayName("markSkipped() should throw IllegalStateException when status is not READY")
        void throwIllegalStateException_notReady() {
            for (JobStatus status : JobStatus.values()) {
                if (status == JobStatus.READY) continue;
                StepExecutionStatusEntity entity = createEntityWithStatus(status);
                IllegalStateException ex = assertThrows(
                        IllegalStateException.class,
                        entity::markSkipped
                );
                assertEquals("Execution must be READY to be marked skipped.", ex.getMessage());
            }

        }
    }

    @Nested
    @DisplayName("pause()")
    class PauseTests {

        @Test
        @DisplayName("pause() should transition to PAUSED when status is RUNNING")
        void pauseWhenRunning() {
            StepExecutionStatusEntity entity = createEntityWithStatus(JobStatus.RUNNING);

            entity.pause();
            assertEquals(JobStatus.PAUSED, entity.getStatus());
        }

        @Test
        @DisplayName("markSkipped() should throw IllegalStateException when status is not RUNNING")
        void throwIllegalStateException_notRunning() {
            for (JobStatus status : JobStatus.values()) {
                if (status == JobStatus.RUNNING) continue;
                StepExecutionStatusEntity entity = createEntityWithStatus(status);
                IllegalStateException ex = assertThrows(
                        IllegalStateException.class,
                        entity::pause
                );
                assertEquals("Execution must be RUNNING to pause.", ex.getMessage());
            }

        }
    }

    @Nested
    @DisplayName("resume()")
    class ResumeTests {

        @Test
        @DisplayName("resume() should transition to RUNNING when status is PAUSED")
        void resumeWhenPaused() {
            StepExecutionStatusEntity entity = createEntityWithStatus(JobStatus.PAUSED);

            entity.resume();
            assertEquals(JobStatus.RUNNING, entity.getStatus());
        }

        @Test
        @DisplayName("resume() should throw IllegalStateException when status is not PAUSED")
        void throwIllegalStateException_notPaused() {
            for (JobStatus status : JobStatus.values()) {
                if (status == JobStatus.PAUSED) continue;
                StepExecutionStatusEntity entity = createEntityWithStatus(status);
                IllegalStateException ex = assertThrows(
                        IllegalStateException.class,
                        entity::resume
                );
                assertEquals("Execution must be PAUSED to resume.", ex.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("isTerminal()")
    class IsTerminalTests {

        @Test
        @DisplayName("isTerminal() should be false for READY and RUNNING")
        void nonTerminalStatuses() {
            StepExecutionStatusEntity ready = createEntityWithStatus(JobStatus.READY);
            StepExecutionStatusEntity running = createEntityWithStatus(JobStatus.RUNNING);
            StepExecutionStatusEntity paused = createEntityWithStatus(JobStatus.PAUSED);

            assertFalse(ready.isTerminal(), "READY should not be terminal");
            assertFalse(running.isTerminal(), "RUNNING should not be terminal");
            assertFalse(paused.isTerminal(), "PAUSED should not be terminal");
        }

        @Test
        @DisplayName("isTerminal() should be true for COMPLETED, FAILED and TERMINATED")
        void terminalStatuses() {
            StepExecutionStatusEntity completed = createEntityWithStatus(JobStatus.COMPLETED);
            StepExecutionStatusEntity failed = createEntityWithStatus(JobStatus.FAILED);
            StepExecutionStatusEntity terminated = createEntityWithStatus(JobStatus.TERMINATED);
            StepExecutionStatusEntity skipped = createEntityWithStatus(JobStatus.SKIPPED);

            assertTrue(completed.isTerminal(), "COMPLETED should be terminal");
            assertTrue(failed.isTerminal(), "FAILED should be terminal");
            assertTrue(terminated.isTerminal(), "TERMINATED should be terminal");
            assertTrue(skipped.isTerminal(), "SKIPPED should be terminal");
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
}