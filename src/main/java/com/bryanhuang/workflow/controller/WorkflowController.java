package com.bryanhuang.workflow.controller;

import com.bryanhuang.workflow.dto.request.CreateWorkflowRequest;
import com.bryanhuang.workflow.dto.response.CreateWorkflowResponse;
import com.bryanhuang.workflow.dto.response.WorkflowResponse;
import com.bryanhuang.workflow.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
@Slf4j
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping
    public ResponseEntity<CreateWorkflowResponse> createWorkflow(
            @Valid @RequestBody CreateWorkflowRequest createWorkflowRequest
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
