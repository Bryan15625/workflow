package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.model.workflow.JobControl;
import com.bryanhuang.workflow.model.workflow.Step;
import com.bryanhuang.workflow.model.workflow.StepName;
import com.bryanhuang.workflow.model.workflow.Workflow;
import com.bryanhuang.workflow.service.workout.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StepExecutorServiceTest {

    @Mock
    private WorkoutCsvIngestionService workoutCsvIngestionService;

    @Mock
    private WorkoutAggregationService workoutAggregationService;

    @Mock
    private WorkoutMetricsEvaluationService workoutMetricsEvaluationService;

    @Mock
    private WorkoutSummaryGenerationService workoutSummaryGenerationService;

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
            when(workoutCsvIngestionService.ingestCsv(step, workflow, workflowExecutionEntity))
                    .thenReturn(JobControl.NONE);

            JobControl result = stepExecutorService.execute(step, workflow, workflowExecutionEntity);

            assertEquals(JobControl.NONE, result);
            verify(workoutCsvIngestionService).ingestCsv(step, workflow, workflowExecutionEntity);
        }

        @Test
        @DisplayName("delegates the aggregate data step to the downstream class")
        void aggregateData_DelegatesDownstream() throws InterruptedException {
            Step step = mock(Step.class);
            Workflow workflow = mock(Workflow.class);
            WorkflowExecutionEntity workflowExecutionEntity = mock(WorkflowExecutionEntity.class);

            when(step.getStepName()).thenReturn(StepName.valueOf(StepName.AGGREGATE_DATA.name()));
            when(workoutAggregationService.aggregateWorkoutData(step, workflowExecutionEntity))
                    .thenReturn(JobControl.NONE);

            JobControl result = stepExecutorService.execute(step, workflow, workflowExecutionEntity);

            assertEquals(JobControl.NONE, result);
            verify(workoutAggregationService).aggregateWorkoutData(step, workflowExecutionEntity);
        }

        @Test
        @DisplayName("delegates the metrics evaluation step to the downstream class")
        void evaluateMetrics_DelegatesDownstream() throws InterruptedException {
            Step step = mock(Step.class);
            Workflow workflow = mock(Workflow.class);
            WorkflowExecutionEntity workflowExecutionEntity = mock(WorkflowExecutionEntity.class);

            when(step.getStepName()).thenReturn(StepName.valueOf(StepName.EVALUATE_METRICS.name()));
            when(workoutMetricsEvaluationService.evaluateMetrics(step, workflow, workflowExecutionEntity))
                    .thenReturn(JobControl.NONE);

            JobControl result = stepExecutorService.execute(step, workflow, workflowExecutionEntity);

            assertEquals(JobControl.NONE, result);
            verify(workoutMetricsEvaluationService).evaluateMetrics(step, workflow, workflowExecutionEntity);
        }

        @Test
        @DisplayName("delegates the generate summary step to the downstream class")
        void generateSummary_DelegatesDownstream() throws InterruptedException {
            Step step = mock(Step.class);
            Workflow workflow = mock(Workflow.class);
            WorkflowExecutionEntity workflowExecutionEntity = mock(WorkflowExecutionEntity.class);

            when(step.getStepName()).thenReturn(StepName.valueOf(StepName.GENERATE_SUMMARY.name()));
            when(workoutSummaryGenerationService.generateSummary(step, workflow, workflowExecutionEntity))
                    .thenReturn(JobControl.NONE);

            JobControl result = stepExecutorService.execute(step, workflow, workflowExecutionEntity);

            assertEquals(JobControl.NONE, result);
            verify(workoutSummaryGenerationService).generateSummary(step, workflow, workflowExecutionEntity);
        }

    }
}
