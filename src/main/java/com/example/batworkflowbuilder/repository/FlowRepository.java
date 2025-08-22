package com.example.batworkflowbuilder.repository;

import com.example.batworkflowbuilder.entity.Flow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FlowRepository extends JpaRepository<Flow, Integer>, JpaSpecificationExecutor<Flow> {
}