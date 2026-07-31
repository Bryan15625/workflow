package com.bryanhuang.workflow.model.workflow;

import com.bryanhuang.workflow.model.CohortProfile;
import com.bryanhuang.workflow.model.Data;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class Workflow {
    private UUID workflowId;
    private String workflowName;
    private CohortProfile cohortProfile;
    private Data data;
    private List<Step> steps;
    private Instant createdAt;
    private Instant updatedAt;

    public Workflow(
            UUID workflowId,
            String workflowName,
            CohortProfile cohortProfile,
            Data data,
            List<Step> steps,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.workflowId = workflowId;
        this.workflowName = workflowName;
        this.cohortProfile = cohortProfile;
        this.data = data;
        this.steps = steps;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
