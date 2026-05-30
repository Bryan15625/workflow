package com.bryanhuang.workflow.persistence;

import com.bryanhuang.workflow.model.Input;
import com.bryanhuang.workflow.model.Profile;
import com.bryanhuang.workflow.model.Step;

import java.util.List;

public record WorkflowPayload(
        Profile profile,
        Input input,
        List<Step> steps
) {}
