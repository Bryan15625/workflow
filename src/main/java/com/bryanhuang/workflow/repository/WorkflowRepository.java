package com.bryanhuang.workflow.repository;

import com.bryanhuang.workflow.entity.WorkflowEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkflowRepository extends JpaRepository<WorkflowEntity, UUID> {
}
