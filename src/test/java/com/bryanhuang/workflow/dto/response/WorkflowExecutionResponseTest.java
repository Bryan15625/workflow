package com.bryanhuang.workflow.dto.response;

import com.bryanhuang.workflow.model.workflow.JobStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class WorkflowExecutionResponseTest {

    private WorkflowExecutionResponse buildResponse(JobStatus status) {
        return WorkflowExecutionResponse.builder()
                .workflowExecutionId(UUID.randomUUID())
                .workflowId(UUID.randomUUID())
                .status(status)
                .stepStatuses(null)
                .build();
    }

    @Nested
    @DisplayName("getNote()")
    class GetNoteTests {

        @Test
        @DisplayName("returns a rollback note when status is FAILED")
        void returnsNote_whenFailed() {
            WorkflowExecutionResponse response = buildResponse(JobStatus.FAILED);

            assertNotNull(response.getNote());
        }

        @Test
        @DisplayName("returns a rollback note when status is TERMINATED")
        void returnsNote_whenTerminated() {
            WorkflowExecutionResponse response = buildResponse(JobStatus.TERMINATED);

            assertNotNull(response.getNote());
        }

        @Test
        @DisplayName("returns the same note text for both FAILED and TERMINATED")
        void returnsConsistentNoteText_acrossFailedAndTerminated() {
            String failedNote = buildResponse(JobStatus.FAILED).getNote();
            String terminatedNote = buildResponse(JobStatus.TERMINATED).getNote();

            assertEquals(failedNote, terminatedNote);
        }

        @ParameterizedTest
        @EnumSource(value = JobStatus.class, names = {"FAILED", "TERMINATED"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("returns null for every non-terminal-failure status")
        void returnsNull_forAllOtherStatuses(JobStatus status) {
            WorkflowExecutionResponse response = buildResponse(status);

            assertNull(response.getNote());
        }
    }
}