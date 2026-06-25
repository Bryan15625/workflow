package com.bryanhuang.workflow.service;

import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;

import com.bryanhuang.workflow.model.Step;
import com.bryanhuang.workflow.model.Workflow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskExecutionService {
    private final WorkflowQueryService workflowQueryService;
    public void executeWorkflow(WorkflowExecutionEntity entity) {
        log.info("Executing workflow");
        try {
            Workflow workflow = workflowQueryService.findWorkflow(entity.getWorkflowId());
//            for (Step step : workflow.getSteps()) {
//                executeStep(step);
//
//            }
            entity.complete();
            log.info("Task execution completed");
        } catch (Exception e) {
            entity.fail();
        }
    }
    public void executeStep(Step step) {
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {}
    }
}
