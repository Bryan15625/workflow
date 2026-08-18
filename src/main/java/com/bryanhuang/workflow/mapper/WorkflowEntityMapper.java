package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.entity.workflow.WorkflowEntity;
import com.bryanhuang.workflow.model.workflow.Workflow;
import com.bryanhuang.workflow.entity.payload.WorkflowPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class WorkflowEntityMapper {

    public WorkflowEntity toWorkflowEntity(Workflow workflow) {

        WorkflowPayload payload = WorkflowPayload.builder()
                .cohortProfile(workflow.getCohortProfile())
                .data(workflow.getData())
                .steps(workflow.getSteps())
                .build();

        return WorkflowEntity.builder()
                .workflowId(workflow.getWorkflowId())
                .workflowName(workflow.getWorkflowName())
                .workflowJson(payload)
                .createdAt(workflow.getCreatedAt())
                .updatedAt(workflow.getUpdatedAt())
                .build();
    }

    public Workflow toWorkflow(WorkflowEntity workflowEntity) {

            WorkflowPayload workflowPayload = workflowEntity.getWorkflowJson();

            return Workflow.builder()
                    .workflowId(workflowEntity.getWorkflowId())
                    .workflowName(workflowEntity.getWorkflowName())
                    .cohortProfile(workflowPayload.getCohortProfile())
                    .data(workflowPayload.getData())
                    .steps(workflowPayload.getSteps())
                    .createdAt(workflowEntity.getCreatedAt())
                    .updatedAt(workflowEntity.getUpdatedAt())
                    .build();
    }
}
