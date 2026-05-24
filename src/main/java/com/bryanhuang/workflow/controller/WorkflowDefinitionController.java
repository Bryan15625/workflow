package com.bryanhuang.workflow.controller;

import com.bryanhuang.workflow.dto.request.CreateWorkflowDefinitionRequest;
import com.bryanhuang.workflow.dto.response.CreateWorkflowDefinitionResponse;
import com.bryanhuang.workflow.service.WorkflowDefinitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/definition")
@RequiredArgsConstructor
@Slf4j
public class WorkflowDefinitionController {

    private final WorkflowDefinitionService workflowDefinitionService;

    @PostMapping
    public ResponseEntity<CreateWorkflowDefinitionResponse> createWorkflowDefinition(@Valid @RequestBody CreateWorkflowDefinitionRequest createWorkflowDefinitionRequest) {
        log.info("Received request to create workflow definition for: {}", createWorkflowDefinitionRequest.getWorkflowDefinitionName());

        CreateWorkflowDefinitionResponse response = workflowDefinitionService
                .createWorkflowDefinition(createWorkflowDefinitionRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

}
