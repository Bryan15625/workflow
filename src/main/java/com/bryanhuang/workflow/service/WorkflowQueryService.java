package com.bryanhuang.workflow.service;

import com.bryanhuang.workflow.entity.WorkflowEntity;
import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import com.bryanhuang.workflow.exception.WorkflowExecutionNotFoundException;
import com.bryanhuang.workflow.exception.WorkflowNotFoundException;
import com.bryanhuang.workflow.mapper.WorkflowEntityMapper;
import com.bryanhuang.workflow.model.Workflow;
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
    private final WorkflowRepository workflowRepository;

    public Workflow findWorkflowEntityAndMapToWorkflow(UUID workflowId)  {
        WorkflowEntity workflowEntity = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new WorkflowNotFoundException(
                        "Workflow not found with id: " + workflowId
                ));
        return workflowEntityMapper.toWorkflow(workflowEntity);
    }

}
