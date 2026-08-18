package com.bryanhuang.workflow.entity.workflow;

import com.bryanhuang.workflow.entity.payload.WorkflowPayload;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflow")
@Getter
@NoArgsConstructor
@Builder
public class WorkflowEntity {

    @Id
    private UUID workflowId;
    private String workflowName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private WorkflowPayload workflowJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public WorkflowEntity(
            UUID workflowId,
            String workflowName,
            WorkflowPayload workflowJson,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.workflowId = workflowId;
        this.workflowName = workflowName;
        this.workflowJson = workflowJson;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
