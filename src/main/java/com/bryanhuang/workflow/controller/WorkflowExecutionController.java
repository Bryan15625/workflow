package com.bryanhuang.workflow.controller;

import com.bryanhuang.workflow.dto.response.CreateWorkflowExecutionResponse;
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

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionController {

    private final WorkflowExecutionService workflowExecutionService;

    @PostMapping("/workflows/{workflowId}/executions")
    public ResponseEntity<CreateWorkflowExecutionResponse> createWorkflowExecution(@PathVariable UUID workflowId) {
        log.info("Received request to execute workflow with ID={}", workflowId);

        CreateWorkflowExecutionResponse response = workflowExecutionService
                .createWorkflowExecution(workflowId);

        URI location = URI.create(
                "/executions/" + response.workflowExecutionId()
        );

        return ResponseEntity
                .created(location)
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