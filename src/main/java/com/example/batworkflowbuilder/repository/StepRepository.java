package com.example.batworkflowbuilder.repository;

import com.example.batworkflowbuilder.entity.Step;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StepRepository extends JpaRepository<Step, Integer> {
}
