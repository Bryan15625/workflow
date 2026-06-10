package com.bryanhuang.workflow.service;

import com.bryanhuang.workflow.entity.WorkflowEntity;
import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import com.bryanhuang.workflow.exception.WorkflowExecutionNotFoundException;
import com.bryanhuang.workflow.exception.WorkflowNotFoundException;
import com.bryanhuang.workflow.mapper.WorkflowEntityMapper;
import com.bryanhuang.workflow.mapper.WorkflowExecutionEntityMapper;
import com.bryanhuang.workflow.model.Workflow;
import com.bryanhuang.workflow.model.WorkflowExecution;
import com.bryanhuang.workflow.repository.WorkflowExecutionRepository;
import com.bryanhuang.workflow.repository.WorkflowRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowQueryService {

    private final WorkflowEntityMapper workflowEntityMapper;
    private final WorkflowExecutionEntityMapper workflowExecutionEntityMapper;
    private final WorkflowRepository workflowRepository;
    private final WorkflowExecutionRepository workflowExecutionRepository;

    public Workflow findWorkflow(UUID workflowId)  {
        WorkflowEntity workflowEntity = getWorkflowById(workflowId);
        return workflowEntityMapper.toWorkflow(workflowEntity);
    }

    private WorkflowEntity getWorkflowById(UUID workflowId) {
        return workflowRepository.findById(workflowId)
                .orElseThrow(() -> new WorkflowNotFoundException(
                        "Workflow not found with id: " + workflowId
                ));
    }

    public WorkflowExecution findWorkflowExecution(UUID workflowExecutionId) {
        WorkflowExecutionEntity entity = getWorkflowExecutionById(workflowExecutionId);
        return workflowExecutionEntityMapper.toWorkflowExecution(entity);
    }

    // TODO: replace error with custom exception
    private WorkflowExecutionEntity getWorkflowExecutionById(UUID workflowExecutionId) {
        return workflowExecutionRepository.findById(workflowExecutionId)
                .orElseThrow(() -> new WorkflowExecutionNotFoundException(
                        "Workflow execution not found with id: " + workflowExecutionId
                ));
    }

    // TODO: check if workflow name already exists and throw exception if it does
    @Transactional
    public WorkflowEntity saveWorkflowEntity(WorkflowEntity workflowEntity) {

        return workflowRepository.save(workflowEntity);
        // Ensure workflow name isn't already used in the DB

    }

    @Transactional
    public WorkflowExecutionEntity saveWorkflowExecutionEntity(WorkflowExecutionEntity workflowExecutionEntity) {

        return workflowExecutionRepository.save(workflowExecutionEntity);
        // Ensure workflow name isn't already used in the DB

    }
}
