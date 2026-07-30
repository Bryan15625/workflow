package com.bryanhuang.workflow.controller;

import com.bryanhuang.workflow.dto.request.CreateWorkflowRequest;
import com.bryanhuang.workflow.dto.response.CreateWorkflowResponse;
import com.bryanhuang.workflow.dto.response.WorkflowResponse;
import com.bryanhuang.workflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Workflows",
        description = "Define new workflows and retrieve existing workflow definitions."
)
@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
@Slf4j
public class WorkflowController {

    private final WorkflowService workflowService;

    @Operation(
            summary = "Create a new workflow",
            description = "Creates a workflow definition given a cohort profile, input schema, and ordered steps."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Workflow created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid workflow definition"
            )
    })
    @PostMapping
    public ResponseEntity<CreateWorkflowResponse> createWorkflow(
            @Valid
            @RequestBody
            CreateWorkflowRequest createWorkflowRequest
    ) {
        log.info("Received request to create workflow for: {}",
                createWorkflowRequest.getWorkflowName()
        );

        CreateWorkflowResponse response = workflowService
                .createWorkflow(createWorkflowRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Get a workflow by ID",
            description = "Returns the workflow definition, including steps and metadata, for the given workflow ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workflow found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workflow not found"
            )
    })
    @GetMapping("/{workflowId}")
    public ResponseEntity<WorkflowResponse> getWorkflow(@PathVariable UUID workflowId) {
        log.info("Received request to get workflow for workflowId={}", workflowId);

        WorkflowResponse response = workflowService
                .getWorkflow(workflowId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

}
