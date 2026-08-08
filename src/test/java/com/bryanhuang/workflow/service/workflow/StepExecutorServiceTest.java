package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import com.bryanhuang.workflow.model.workflow.Step;
import com.bryanhuang.workflow.model.workflow.StepName;
import com.bryanhuang.workflow.model.workflow.Workflow;
import com.bryanhuang.workflow.service.workout.WorkoutCsvIngestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StepExecutorServiceTest {

    @Mock
    private WorkoutCsvIngestionService workoutCsvIngestionService;

    @InjectMocks
    private StepExecutorService stepExecutorService;

    @Nested
    @DisplayName("execute()")
    class ExecuteTests {

        @Test
        @DisplayName("delegates the ingest csv step to the downstream class")
        void ingestCsv_DelegatesDownstream() throws InterruptedException {
            Step step = mock(Step.class);
            Workflow workflow = mock(Workflow.class);
            WorkflowExecutionEntity workflowExecutionEntity = mock(WorkflowExecutionEntity.class);

            when(step.getStepName()).thenReturn(StepName.valueOf(StepName.INGEST_CSV.name()));

            stepExecutorService.execute(step, workflow, workflowExecutionEntity);

            // TODO: verify service is called once implemented
        }

        @Test
        @DisplayName("delegates the aggregate data step to the downstream class")
        void aggregateData_DelegatesDownstream() throws InterruptedException {
            Step step = mock(Step.class);
            Workflow workflow = mock(Workflow.class);
            WorkflowExecutionEntity workflowExecutionEntity = mock(WorkflowExecutionEntity.class);

            when(step.getStepName()).thenReturn(StepName.valueOf(StepName.AGGREGATE_DATA.name()));

            stepExecutorService.execute(step, workflow, workflowExecutionEntity);

            // TODO: verify service is called once implemented
        }

        @Test
        @DisplayName("delegates the evaluate metrics step to the downstream class")
        void evaluateMetrics_DelegatesDownstream() throws InterruptedException {
            Step step = mock(Step.class);
            Workflow workflow = mock(Workflow.class);
            WorkflowExecutionEntity workflowExecutionEntity = mock(WorkflowExecutionEntity.class);

            when(step.getStepName()).thenReturn(StepName.valueOf(StepName.EVALUATE_METRICS.name()));

            stepExecutorService.execute(step, workflow, workflowExecutionEntity);

            // TODO: verify service is called once implemented
        }

        @Test
        @DisplayName("delegates the generate summary step to the downstream class")
        void generateSummary_DelegatesDownstream() throws InterruptedException {
            Step step = mock(Step.class);
            Workflow workflow = mock(Workflow.class);
            WorkflowExecutionEntity workflowExecutionEntity = mock(WorkflowExecutionEntity.class);

            when(step.getStepName()).thenReturn(StepName.valueOf(StepName.GENERATE_SUMMARY.name()));

            stepExecutorService.execute(step, workflow, workflowExecutionEntity);

            // TODO: verify service is called once implemented
        }

        @Test
        @DisplayName("throws IllegalArgumentException when invalid step name is passed")
        void default_throwsIllegalArgumentException() throws InterruptedException {
            Step step = mock(Step.class);
            Workflow workflow = mock(Workflow.class);
            WorkflowExecutionEntity workflowExecutionEntity = mock(WorkflowExecutionEntity.class);

            when(step.getStepName()).thenReturn(null);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> stepExecutorService.execute(step, workflow, workflowExecutionEntity),
                    "Invalid step name"
            );

            // TODO: verify service is called once implemented
        }

    }
}
