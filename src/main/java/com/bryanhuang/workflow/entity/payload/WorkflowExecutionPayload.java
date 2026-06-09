package com.bryanhuang.workflow.entity.payload;

import com.bryanhuang.workflow.model.StepExecutionStatus;

import java.util.List;

public record WorkflowExecutionPayload(
        List<StepExecutionStatus> stepStatuses
) {}
