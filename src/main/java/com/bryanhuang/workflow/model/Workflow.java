package com.bryanhuang.workflow.model;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class Workflow {
    private UUID workflowId;
    private String workflowName;
    private Profile profile;
    private Data data;
    private List<Step> steps;
    private Instant createdAt;
    private Instant updatedAt;

    public Workflow(
            UUID workflowId,
            String workflowName,
            Profile profile,
            Data data,
            List<Step> steps,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.workflowId = workflowId;
        this.workflowName = workflowName;
        this.profile = profile;
        this.data = data;
        this.steps = steps;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
