package com.bryanhuang.workflow.dto.response;

import com.bryanhuang.workflow.model.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@AllArgsConstructor
@Getter
public class WorkflowExecutionResponse {
    private final String workflowExecutionId;
    private final String workflowId;
    private final JobStatus jobStatus;
    private final Instant startTime;
    private final Instant endTime;
}
