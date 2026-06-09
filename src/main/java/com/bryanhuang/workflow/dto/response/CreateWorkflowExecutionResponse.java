package com.bryanhuang.workflow.dto.response;


import java.util.UUID;

public record CreateWorkflowExecutionResponse(
        UUID workflowExecutionId
) {}
