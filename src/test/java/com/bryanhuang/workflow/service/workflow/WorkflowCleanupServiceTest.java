package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.repository.WorkoutRecordRepository;
import com.bryanhuang.workflow.repository.WorkoutUserAggregateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkflowCleanupServiceTest {

    @Mock
    private WorkoutRecordRepository workoutRecordRepository;

    @Mock
    private WorkoutUserAggregateRepository workoutUserAggregateRepository;

    @InjectMocks
    private WorkflowCleanupService workflowCleanupService;

    @Nested
    @DisplayName("cleanupWorkflowExecution()")
    class CleanupWorkflowExecutionTests {

        @Test
        @DisplayName("calls delete on the rows associated with the workflowExecutionId")
        void callsDeleteOnWorkoutRecordRepository() {
            UUID workflowExecutionId = UUID.randomUUID();
            workflowCleanupService.cleanupWorkflowExecution(workflowExecutionId);

            verify(workoutRecordRepository).deleteByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId);
            verify(workoutUserAggregateRepository).deleteByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId);
        }
    }
}
