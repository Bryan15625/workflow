package com.bryanhuang.workflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CreateWorkflowDefinitionResponse {
    private final UUID workflowId;
}
