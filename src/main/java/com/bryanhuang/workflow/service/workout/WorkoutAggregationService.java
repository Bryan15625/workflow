package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.WorkoutRecordEntity;
import com.bryanhuang.workflow.model.workflow.JobControl;
import com.bryanhuang.workflow.model.workflow.Step;
import com.bryanhuang.workflow.model.workflow.Workflow;
import com.bryanhuang.workflow.repository.WorkoutRecordRepository;
import com.bryanhuang.workflow.service.workflow.WorkflowControlGate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutAggregationService {

    private final WorkoutRecordRepository workoutRecordRepository;
    private final WorkflowControlGate workflowControlGate;

    public JobControl aggregateWorkoutData(Step step, Workflow workflow, WorkflowExecutionEntity entity) throws InterruptedException {
        log.info("Aggregating workout data");
        UUID workflowExecutionId = entity.getWorkflowExecutionId();

        int batchSize = 10_000;
        Sort sort = Sort.by(Sort.Order.asc("userId"), Sort.Order.asc("date"));
        Pageable pageable = PageRequest.of(0, batchSize, sort);
        Slice<WorkoutRecordEntity> slice;

        do {
            slice = workoutRecordRepository.findSliceByWorkflowExecutionEntity(entity, pageable);
            List<WorkoutRecordEntity> records = slice.getContent();

            if (records.isEmpty()) {
                log.info("No records found for aggregation");
                break;
            }
            log.info("Processing batch of {} records", records.size());

            analyzeWorkoutData(records);

            if (slice.hasNext()) {
                pageable = pageable.next();

                if (workflowControlGate.checkpointStep(workflowExecutionId, step.getStepId()) == JobControl.TERMINATE) {
                    log.info("Aggregation terminated mid-file: {}", workflowExecutionId);
                    return JobControl.TERMINATE;
                }
            }
        } while (slice.hasNext());

        log.info("Aggregation completed");
        return JobControl.NONE;
    }

    public void analyzeWorkoutData(List<WorkoutRecordEntity> records) {
        // Calculate ideal metrics for the cohort
        log.info("Analysing workout data");
    }
}
