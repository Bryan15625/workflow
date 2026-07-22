package com.bryanhuang.workflow.entity.payload;

import com.bryanhuang.workflow.model.Data;
import com.bryanhuang.workflow.model.Profile;
import com.bryanhuang.workflow.model.Step;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WorkflowPayload {
    private Profile profile;
    private Data data;
    private List<Step> steps;

    public WorkflowPayload(
            Profile profile,
            Data data,
            List<Step> steps
    ) {
        this.profile = profile;
        this.data = data;
        this.steps = steps;
    }
}
