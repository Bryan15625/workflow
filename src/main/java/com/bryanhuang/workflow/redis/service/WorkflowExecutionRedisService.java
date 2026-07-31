package com.bryanhuang.workflow.redis.service;

import com.bryanhuang.workflow.model.workflow.JobControl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowExecutionRedisService {

    private final StringRedisTemplate redisTemplate;

    public void setControl(UUID workflowExecutionId, JobControl control) {
        redisTemplate.opsForValue().set(
                getWorkflowExecutionKey(workflowExecutionId),
                control.name()
        );
    }
    public JobControl getControl(UUID workflowExecutionId) {
        String control = redisTemplate.opsForValue().get(
                        getWorkflowExecutionKey(workflowExecutionId)
                );
        return control == null ? null : JobControl.valueOf(control);
    }
    private String getWorkflowExecutionKey(UUID workflowExecutionId) {
        return "workflow_execution:" + workflowExecutionId + ":control";
    }
}
