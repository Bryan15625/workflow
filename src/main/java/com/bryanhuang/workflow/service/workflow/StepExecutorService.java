package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.model.workflow.JobControl;
import com.bryanhuang.workflow.model.workflow.Step;
import com.bryanhuang.workflow.model.workflow.StepName;
import com.bryanhuang.workflow.model.workflow.Workflow;
import com.bryanhuang.workflow.service.workout.WorkoutAggregationService;
import com.bryanhuang.workflow.service.workout.WorkoutCsvIngestionService;
import com.bryanhuang.workflow.service.workout.WorkoutMetricsEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepExecutorService {

    private final WorkoutCsvIngestionService workoutCsvIngestionService;
    private final WorkoutAggregationService workoutAggregationService;
    private final WorkoutMetricsEvaluationService workoutMetricsEvaluationService;

    public JobControl execute(Step step, Workflow workflow, WorkflowExecutionEntity entity) throws InterruptedException {
        StepName stepName = step.getStepName();
        if (stepName == StepName.INGEST_CSV) {
            return workoutCsvIngestionService.ingestCsv(step, workflow, entity);
        } else if (stepName == StepName.AGGREGATE_DATA) {
            return workoutAggregationService.aggregateWorkoutData(step, entity);
        } else if (stepName == StepName.EVALUATE_METRICS) {
            return workoutMetricsEvaluationService.evaluateMetrics(step, workflow, entity);
        } else if (stepName == StepName.GENERATE_SUMMARY) {
            log.info("Not yet implemented");
        }
        return JobControl.NONE;
    }

}
