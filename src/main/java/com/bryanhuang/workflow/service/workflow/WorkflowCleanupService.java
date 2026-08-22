package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.repository.ReportRepository;
import com.bryanhuang.workflow.repository.WorkoutIdealRepository;
import com.bryanhuang.workflow.repository.WorkoutRecordRepository;
import com.bryanhuang.workflow.repository.WorkoutUserAggregateRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowCleanupService {
    private final WorkoutRecordRepository workoutRecordRepository;
    private final WorkoutUserAggregateRepository workoutUserAggregateRepository;
    private final ReportRepository reportRepository;
    private final WorkoutIdealRepository workoutIdealRepository;

    @Transactional
    public void cleanupWorkflowExecution(UUID workflowExecutionId) {
        log.info("Cleaning up workflow execution");
        workoutRecordRepository.deleteByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId);
        workoutUserAggregateRepository.deleteByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId);
        reportRepository.deleteByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId);
        workoutIdealRepository.deleteByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId);
    }
}
