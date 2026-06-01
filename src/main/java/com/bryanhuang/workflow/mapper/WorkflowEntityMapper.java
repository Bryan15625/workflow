package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.entity.WorkflowEntity;
import com.bryanhuang.workflow.model.Workflow;
import com.bryanhuang.workflow.persistence.WorkflowPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class WorkflowEntityMapper {

    public WorkflowEntity toWorkflowEntity(Workflow workflow) {

        WorkflowPayload payload = WorkflowPayload.builder()
                .profile(workflow.getProfile())
                .input(workflow.getInput())
                .steps(workflow.getSteps())
                .build();

        return WorkflowEntity.builder()
                .workflowId(workflow.getWorkflowId())
                .workflowName(workflow.getWorkflowName())
                .workflowJson(payload)
                .build();
    }

    public Workflow toWorkflow(WorkflowEntity workflowEntity) {

            WorkflowPayload workflowPayload = workflowEntity.getWorkflowJson();

            return Workflow.builder()
                    .workflowId(workflowEntity.getWorkflowId())
                    .workflowName(workflowEntity.getWorkflowName())
                    .profile(workflowPayload.getProfile())
                    .input(workflowPayload.getInput())
                    .steps(workflowPayload.getSteps())
                    .createdAt(workflowEntity.getCreatedAt())
                    .updatedAt(workflowEntity.getUpdatedAt())
                    .build();
    }
}
