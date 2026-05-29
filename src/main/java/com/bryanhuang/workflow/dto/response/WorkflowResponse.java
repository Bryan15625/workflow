package com.bryanhuang.workflow.dto.response;

import com.bryanhuang.workflow.dto.request.InputRequest;
import com.bryanhuang.workflow.dto.request.ProfileRequest;
import com.bryanhuang.workflow.dto.request.StepRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class WorkflowResponse {
    private String workflowDefinitionName;
    private ProfileRequest profile;
    private InputRequest input;
    private List<StepRequest> steps;
    private Instant createdAt;
    private Instant updatedAt;
}
