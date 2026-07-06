package com.bryanhuang.workflow.controller;

import com.bryanhuang.workflow.dto.response.CreateWorkflowExecutionResponse;
import com.bryanhuang.workflow.dto.response.WorkflowExecutionResponse;
import com.bryanhuang.workflow.service.WorkflowExecutionOrchestratorService;
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

    private final WorkflowExecutionOrchestratorService workflowExecutionOrchestratorService;

    @PostMapping("/workflows/{workflowId}/executions")
    public ResponseEntity<CreateWorkflowExecutionResponse> createWorkflowExecution(@PathVariable UUID workflowId) {
        log.info("Received request to execute workflow with ID={}", workflowId);

        CreateWorkflowExecutionResponse response = workflowExecutionOrchestratorService
                .createWorkflowExecution(workflowId);

        URI location = URI.create(
                "/workflow-executions/" + response.workflowExecutionId()
        );

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @GetMapping("/workflow-executions/{executionId}")
    public ResponseEntity<WorkflowExecutionResponse> getExecutionStatus(@PathVariable UUID executionId) {
        log.info("Received request to get execution status for executionId={}", executionId);

        WorkflowExecutionResponse response = workflowExecutionOrchestratorService
                .getWorkflowExecutionStatus(executionId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/workflow-executions/{executionId}/pause")
    public ResponseEntity<Void> pauseExecution(@PathVariable UUID executionId) {
        log.info("Received request to pause execution for executionId={}", executionId);

        workflowExecutionOrchestratorService.pauseExecution(executionId);

        return ResponseEntity
                .accepted()
                .build();
    }

    @PostMapping("/workflow-executions/{executionId}/resume")
    public ResponseEntity<Void> resumeExecution(@PathVariable UUID executionId) {
        log.info("Received request to resume execution for executionId={}", executionId);

        workflowExecutionOrchestratorService.resumeExecution(executionId);

        return ResponseEntity
                .accepted()
                .build();
    }

    @PostMapping("/workflow-executions/{executionId}/terminate")
    public ResponseEntity<Void> terminateExecution(@PathVariable UUID executionId) {
        log.info("Received request to terminate execution for executionId={}", executionId);

        workflowExecutionOrchestratorService.terminateExecution(executionId);

        return ResponseEntity
                .accepted()
                .build();
    }


}