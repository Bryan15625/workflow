package com.bryanhuang.workflow.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/definitions")
public class WorkflowDefinitionController {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowDefinitionController.class);

    @GetMapping("/{id}")
    public void getWorkflowDefinition(@PathVariable String id) {
        logger.info("Hit definitions endpoint {}", id);
    }

    @PostMapping
    public String createWorkflowDefinition() {
        return "This is the post mapping";
    }


}
