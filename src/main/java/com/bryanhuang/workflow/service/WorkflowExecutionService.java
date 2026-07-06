package com.bryanhuang.workflow.service;

import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import com.bryanhuang.workflow.exception.WorkflowExecutionNotFoundException;
import com.bryanhuang.workflow.repository.WorkflowExecutionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowExecutionService {
    private final WorkflowExecutionRepository workflowExecutionRepository;

    @Transactional
    public void start(UUID workflowExecutionId) {
        WorkflowExecutionEntity workflowExecutionEntity =
                workflowExecutionRepository
                        .findByWorkflowExecutionId(
                                workflowExecutionId
                        )
                        .orElseThrow(
                                () -> new WorkflowExecutionNotFoundException("Workflow execution not found")
                        );

        workflowExecutionEntity.start();
    }
    @Transactional
    public void complete(UUID workflowExecutionId) {
        WorkflowExecutionEntity workflowExecutionEntity =
                workflowExecutionRepository
                        .findByWorkflowExecutionId(
                                workflowExecutionId
                        )
                        .orElseThrow(
                                () -> new WorkflowExecutionNotFoundException("Workflow execution not found")
                        );

        workflowExecutionEntity.complete();
    }
}
