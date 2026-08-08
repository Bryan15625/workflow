package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import com.bryanhuang.workflow.model.workflow.Step;
import com.bryanhuang.workflow.model.workflow.StepName;
import com.bryanhuang.workflow.model.workflow.Workflow;
import com.bryanhuang.workflow.service.workout.WorkoutCsvIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepExecutorService {

    private final WorkoutCsvIngestionService workoutCsvIngestionService;

    public void execute(Step step, Workflow workflow, WorkflowExecutionEntity entity) throws InterruptedException {
        StepName stepName = step.getStepName();
        if (stepName == StepName.INGEST_CSV) {
            workoutCsvIngestionService.ingestCsv(step, workflow, entity);
        } else if (stepName == StepName.AGGREGATE_DATA) {
            log.info("Not yet implemented");
        } else if (stepName == StepName.EVALUATE_METRICS) {
            log.info("Not yet implemented");
        } else if (stepName == StepName.GENERATE_SUMMARY) {
            log.info("Not yet implemented");
        } else {
            throw new IllegalArgumentException("Invalid step name: " + stepName);
        }
    }

}
