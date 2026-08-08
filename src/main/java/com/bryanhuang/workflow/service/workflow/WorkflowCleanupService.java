package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.repository.WorkoutRecordRepository;
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

    @Transactional
    public void cleanupWorkflowExecution(UUID workflowExecutionId) {
        log.info("Cleaning up workflow execution");
        workoutRecordRepository.deleteByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId);
    }
}
