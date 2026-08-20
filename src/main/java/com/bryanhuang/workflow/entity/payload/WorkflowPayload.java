package com.bryanhuang.workflow.entity.payload;

import com.bryanhuang.workflow.model.Data;
import com.bryanhuang.workflow.model.CohortProfile;
import com.bryanhuang.workflow.model.workflow.Step;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@Builder
@Jacksonized
public class WorkflowPayload {
    private CohortProfile cohortProfile;
    private Data data;
    private List<Step> steps;

    public WorkflowPayload(
            CohortProfile cohortProfile,
            Data data,
            List<Step> steps
    ) {
        this.cohortProfile = cohortProfile;
        this.data = data;
        this.steps = steps;
    }
}
