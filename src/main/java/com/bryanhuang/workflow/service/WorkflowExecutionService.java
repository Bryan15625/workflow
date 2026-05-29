package com.bryanhuang.workflow.service;

import com.bryanhuang.workflow.dto.response.WorkflowExecutionResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WorkflowExecutionService {
    public WorkflowExecutionResponse executeWorkflow(UUID workflowId) {
        return null;
    }

    public WorkflowExecutionResponse getWorkflowExecutionStatus(UUID executionId) {
        return null;
    }
}
