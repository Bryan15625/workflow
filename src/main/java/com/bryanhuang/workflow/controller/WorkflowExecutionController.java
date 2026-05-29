package com.bryanhuang.workflow.controller;

import com.bryanhuang.workflow.dto.response.WorkflowExecutionResponse;
import com.bryanhuang.workflow.service.WorkflowExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionController {

    private final WorkflowExecutionService workflowExecutionService;

    @PostMapping("/workflows/{workflowId}/executions")
    public ResponseEntity<WorkflowExecutionResponse> executeWorkflow(@PathVariable UUID workflowId) {
        log.info("Received request to execute workflow with ID={}", workflowId);

        WorkflowExecutionResponse response = workflowExecutionService
                .executeWorkflow(workflowId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/executions/{executionId}")
    public ResponseEntity<WorkflowExecutionResponse> getExecutionStatus(@PathVariable UUID executionId) {
        log.info("Received request to get execution status for executionId={}", executionId);

        WorkflowExecutionResponse response = workflowExecutionService
                .getWorkflowExecutionStatus(executionId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}