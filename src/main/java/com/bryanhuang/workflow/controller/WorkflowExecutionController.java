package com.bryanhuang.workflow.controller;

import com.bryanhuang.workflow.dto.response.CreateWorkflowExecutionResponse;
import com.bryanhuang.workflow.dto.response.WorkflowExecutionResponse;
import com.bryanhuang.workflow.service.workflow.WorkflowExecutionOrchestratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(
        name = "Workflow Executions",
        description = "Create/start new workflow executions and control currently executing workflow executions."
)
@RestController
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionController {

    private final WorkflowExecutionOrchestratorService workflowExecutionOrchestratorService;

    @Operation(
            summary = "Create/start execution of an existing workflow",
            description = "Starts executing a workflow given a workflow ID, returns an execution ID for future job controls."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Workflow execution created successfully from found workflow"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workflow is not found and is unable to create/start execution"
            )
    })
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

    @Operation(
            summary = "Get the current execution status of a workflow execution",
            description = "Get the execution status of an executing workflow given an execution ID, " +
                    "returns the current execution progress."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workflow execution found successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workflow execution is not found"
            )
    })
    @GetMapping("/workflow-executions/{executionId}")
    public ResponseEntity<WorkflowExecutionResponse> getExecutionStatus(@PathVariable UUID executionId) {
        log.info("Received request to get execution status for executionId={}", executionId);

        WorkflowExecutionResponse response = workflowExecutionOrchestratorService
                .getWorkflowExecutionStatus(executionId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @Operation(
            summary = "Pause the current execution of a workflow execution",
            description = "Pauses a running workflow execution identified by its execution ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Pause request accepted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workflow execution is not found"
            )
    })
    @PostMapping("/workflow-executions/{executionId}/pause")
    public ResponseEntity<Void> pauseExecution(@PathVariable UUID executionId) {
        log.info("Received request to pause execution for executionId={}", executionId);

        workflowExecutionOrchestratorService.pauseExecution(executionId);

        return ResponseEntity
                .accepted()
                .build();
    }

    @Operation(
            summary = "Resume the current execution of a workflow execution",
            description = "Resumes a paused workflow execution identified by its execution ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Resume request accepted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workflow execution is not found"
            )
    })
    @PostMapping("/workflow-executions/{executionId}/resume")
    public ResponseEntity<Void> resumeExecution(@PathVariable UUID executionId) {
        log.info("Received request to resume execution for executionId={}", executionId);

        workflowExecutionOrchestratorService.resumeExecution(executionId);

        return ResponseEntity
                .accepted()
                .build();
    }

    @Operation(
            summary = "Terminate the current execution of a workflow execution",
            description = "Terminates a paused workflow execution identified by its execution ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Terminate request accepted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workflow execution is not found"
            )
    })
    @PostMapping("/workflow-executions/{executionId}/terminate")
    public ResponseEntity<Void> terminateExecution(@PathVariable UUID executionId) {
        log.info("Received request to terminate execution for executionId={}", executionId);

        workflowExecutionOrchestratorService.terminateExecution(executionId);

        return ResponseEntity
                .accepted()
                .build();
    }
}