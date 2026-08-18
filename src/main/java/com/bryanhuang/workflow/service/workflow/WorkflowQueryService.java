package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.entity.workflow.WorkflowEntity;
import com.bryanhuang.workflow.exception.WorkflowNotFoundException;
import com.bryanhuang.workflow.mapper.WorkflowEntityMapper;
import com.bryanhuang.workflow.model.workflow.Workflow;
import com.bryanhuang.workflow.repository.WorkflowRepository;
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
