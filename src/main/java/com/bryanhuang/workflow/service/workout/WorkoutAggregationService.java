package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.workout.WorkoutRecordEntity;
import com.bryanhuang.workflow.entity.workout.WorkoutUserAggregateEntity;
import com.bryanhuang.workflow.mapper.WorkoutUserAggregateEntityMapper;
import com.bryanhuang.workflow.model.workflow.JobControl;
import com.bryanhuang.workflow.model.workflow.Step;
import com.bryanhuang.workflow.model.workout.WorkoutUserAggregate;
import com.bryanhuang.workflow.repository.WorkoutRecordRepository;
import com.bryanhuang.workflow.repository.WorkoutUserAggregateRepository;
import com.bryanhuang.workflow.service.workflow.WorkflowControlGate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutAggregationService {

    private final WorkoutRecordRepository workoutRecordRepository;
    private final WorkoutUserAggregateRepository workoutUserAggregateRepository;
    private final WorkoutUserAggregateEntityMapper workoutUserAggregateEntityMapper;
    private final WorkflowControlGate workflowControlGate;

    private static final int READ_BATCH_SIZE = 10_000;
    private static final int SAVE_BATCH_SIZE = 10_000;

    public JobControl aggregateWorkoutData(Step step, WorkflowExecutionEntity entity) throws InterruptedException {
        UUID workflowExecutionId = entity.getWorkflowExecutionId();
        log.info("Aggregating workout data for workflow execution: {}", workflowExecutionId);

        Sort sort = Sort.by(Sort.Order.asc("userId"), Sort.Order.asc("date"));
        Pageable pageable = PageRequest.of(0, READ_BATCH_SIZE, sort);
        Slice<WorkoutRecordEntity> slice;

        WorkoutUserAggregateAccumulator currentAccumulator = null;
        List<WorkoutUserAggregateEntity> pendingSave = new ArrayList<>(SAVE_BATCH_SIZE);

        do {
            slice = workoutRecordRepository.findSliceByWorkflowExecutionEntity(entity, pageable);
            List<WorkoutRecordEntity> records = slice.getContent();

            if (records.isEmpty()) {
                log.info("No records found for aggregation");
                break;
            }
            log.info("Processing batch of {} records on step {} for workflowExecutionId {}", records.size(),
                    step.getStepId(), workflowExecutionId);

            for (WorkoutRecordEntity record : records) {
                if (currentAccumulator == null || !currentAccumulator.getUserId().equals(record.getUserId())) {
                    // Moving to a new user means the previous one is fully done
                    // (records are sorted by userId, so no user reappears later).
                    if (currentAccumulator != null) {
                        pendingSave.add(toEntity(currentAccumulator, entity));
                        if (pendingSave.size() >= SAVE_BATCH_SIZE) {
                            workoutUserAggregateRepository.saveAll(pendingSave);
                            pendingSave.clear();
                        }
                    }
                    currentAccumulator = new WorkoutUserAggregateAccumulator(record.getUserId());
                }
                currentAccumulator.accumulate(record);
            }

            if (slice.hasNext()) {
                pageable = pageable.next();

                if (workflowControlGate.checkpointStep(workflowExecutionId, step.getStepId()) == JobControl.TERMINATE) {
                    log.info("Aggregation terminated mid-run: {}", workflowExecutionId);
                    return JobControl.TERMINATE;
                }
            }
        } while (slice.hasNext());

        if (currentAccumulator != null) {
            pendingSave.add(toEntity(currentAccumulator, entity));
        }
        if (!pendingSave.isEmpty()) {
            workoutUserAggregateRepository.saveAll(pendingSave);
        }

        log.info("Aggregation completed");
        return JobControl.NONE;
    }

    private WorkoutUserAggregateEntity toEntity(WorkoutUserAggregateAccumulator acc, WorkflowExecutionEntity entity) {
        WorkoutUserAggregate aggregate = acc.toModel();
        return workoutUserAggregateEntityMapper.toWorkoutUserAggregateEntity(aggregate, entity);
    }
}