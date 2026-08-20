package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.workout.ReportEntity;
import com.bryanhuang.workflow.entity.workout.WorkoutUserAggregateEntity;
import com.bryanhuang.workflow.mapper.ReportEntityMapper;
import com.bryanhuang.workflow.mapper.WorkoutUserAggregateEntityMapper;
import com.bryanhuang.workflow.model.CohortProfile;
import com.bryanhuang.workflow.model.workflow.JobControl;
import com.bryanhuang.workflow.model.workflow.Step;
import com.bryanhuang.workflow.model.workflow.Workflow;
import com.bryanhuang.workflow.model.workout.CohortAnalysisResult;
import com.bryanhuang.workflow.model.workout.WorkoutIdeal;
import com.bryanhuang.workflow.model.workout.WorkoutUserAggregate;
import com.bryanhuang.workflow.repository.ReportRepository;
import com.bryanhuang.workflow.repository.WorkoutUserAggregateRepository;
import com.bryanhuang.workflow.service.workflow.WorkflowControlGate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutMetricsEvaluationService {

    private final WorkoutUserAggregateRepository workoutUserAggregateRepository;
    private final WorkflowControlGate workflowControlGate;
    private final WorkoutIdealsCalculator workoutIdealsCalculator;
    private final WorkoutUserAggregateEntityMapper workoutUserAggregateEntityMapper;
    private final ReportRepository reportRepository;
    private final ReportEntityMapper reportEntityMapper;

    private static final int BATCH_SIZE = 10_000;


    public JobControl evaluateMetrics(Step step, Workflow workflow, WorkflowExecutionEntity entity)
            throws InterruptedException {
        UUID workflowExecutionId = entity.getWorkflowExecutionId();
        log.info("Evaluating metrics for workflow execution: {}", workflowExecutionId);
        CohortProfile cohortProfile = workflow.getCohortProfile();

        Pageable pageable = PageRequest.of(0, BATCH_SIZE);
        Slice<WorkoutUserAggregateEntity> slice;
        CohortMetricsAccumulator cohortAccumulator = new CohortMetricsAccumulator(cohortProfile.getGoal(),
                cohortProfile.getWeightKg());

        // First, determine ideal results at the end of the time period, given the goal
        WorkoutIdeal ideal = workoutIdealsCalculator.calculateIdeal(cohortProfile);

        do {
            slice = workoutUserAggregateRepository.findSliceByWorkflowExecutionEntity(entity, pageable);
            List<WorkoutUserAggregateEntity> aggregates = slice.getContent();

            if (aggregates.isEmpty()) {
                log.info("No records found for evaluation");
                break;
            }
            log.info("Processing batch of {} aggregates on step {} for workflowExecutionId {}", aggregates.size(),
                    step.getStepId(), workflowExecutionId);

            // Compare each user to the ideal metrics
            for (WorkoutUserAggregateEntity aggregateEntity : aggregates) {
                WorkoutUserAggregate actual =
                        workoutUserAggregateEntityMapper.toWorkoutUserAggregate(aggregateEntity);
                cohortAccumulator.record(actual, ideal);
            }
            if (slice.hasNext()) {
                pageable = pageable.next();

                if (workflowControlGate.checkpointStep(workflowExecutionId, step.getStepId()) == JobControl.TERMINATE) {
                    log.info("Metrics evaluation terminated mid-run: {}", workflowExecutionId);
                    return JobControl.TERMINATE;
                }
            }

        } while (slice.hasNext());

        // Save the report for next step
        CohortAnalysisResult.Report report = cohortAccumulator.toReport(cohortProfile.getDurationDays());
        ReportEntity reportEntity = reportEntityMapper.toReportEntity(report, entity);
        reportRepository.save(reportEntity);

        log.info("Metrics evaluation completed");
        return JobControl.NONE;
    }

}
