package com.bryanhuang.workflow.persistence;

import com.bryanhuang.workflow.model.Input;
import com.bryanhuang.workflow.model.Profile;
import com.bryanhuang.workflow.model.Step;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WorkflowPayload {
    private Profile profile;
    private Input input;
    private List<Step> steps;

    public WorkflowPayload(
            Profile profile,
            Input input,
            List<Step> steps
    ) {
        this.profile = profile;
        this.input = input;
        this.steps = steps;
    }
}
