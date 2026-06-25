package com.bryanhuang.workflow.entity;


import com.bryanhuang.workflow.model.JobStatus;
import com.bryanhuang.workflow.entity.payload.WorkflowExecutionPayload;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflow_execution")
@Getter
@NoArgsConstructor
@Builder
public class WorkflowExecutionEntity {

    @Id
    private UUID workflowExecutionId;
    private UUID workflowId;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private WorkflowExecutionPayload executionPayload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private Instant startedAt;

    private Instant completedAt;

    public WorkflowExecutionEntity(
            UUID workflowExecutionId,
            UUID workflowId,
            JobStatus status,
            WorkflowExecutionPayload executionPayload,
            Instant createdAt,
            Instant startedAt,
            Instant completedAt
    ) {
        this.workflowExecutionId = workflowExecutionId;
        this.workflowId = workflowId;
        this.status = status;
        this.executionPayload = executionPayload;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public void start() {

        if (status != JobStatus.READY) {
            throw new IllegalStateException(
                    "Execution must be READY"
            );
        }
        status = JobStatus.RUNNING;
        startedAt = Instant.now();
    }

    public void fail() {
        if (status == JobStatus.COMPLETED || status == JobStatus.TERMINATED) {
            throw new IllegalStateException(
                    "Cannot fail an execution that is already completed or terminated"
            );
        }
        status = JobStatus.FAILED;
    }

    public void complete() {
        if (status == JobStatus.RUNNING) {
            status = JobStatus.COMPLETED;
            completedAt = Instant.now();
        } else {
            throw new IllegalStateException(
                    "Cannot complete an execution that is already completed or terminated"
            );
        }
    }

    public void terminate() {
        if (status == JobStatus.RUNNING) {
            status = JobStatus.TERMINATED;
        } else {
            throw new IllegalStateException(
                    "Cannot terminate an execution that is already completed or terminated"
            );
        }
    }

}
