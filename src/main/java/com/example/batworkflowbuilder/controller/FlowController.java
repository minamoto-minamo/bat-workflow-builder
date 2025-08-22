package com.example.batworkflowbuilder.controller;

import com.example.batworkflowbuilder.dao.specification.FlowSpecification;
import com.example.batworkflowbuilder.dto.FlowRequestDto;
import com.example.batworkflowbuilder.entity.Flow;
import com.example.batworkflowbuilder.repository.FlowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/flow")
@CrossOrigin
public class FlowController {

	@Autowired
	private FlowRepository repository;

	@GetMapping
	public List<Flow> getAll() {
		return repository.findAll(
				FlowSpecification.notLogicalDeleted()
		);
	}

	@PostMapping
	public Flow create(@RequestBody FlowRequestDto dto) {
		Flow flow = new Flow();
		flow.setName(dto.getName());
		flow.setMemo(dto.getMemo());
		flow.setFlowJson(dto.getFlowJson());
		flow.setCreatedAt(LocalDateTime.now());
		flow.setUpdatedAt(LocalDateTime.now());
		return repository.save(flow);
	}

	@PutMapping("/{id}")
	public Flow update(@PathVariable Integer id, @RequestBody FlowRequestDto dto) {
		return repository.findById(id).map(flow -> {
			flow.setName(dto.getName());
			flow.setMemo(dto.getMemo());
			flow.setFlowJson(dto.getFlowJson());
			flow.setUpdatedAt(LocalDateTime.now());
			return repository.save(flow);
		}).orElseThrow(() -> new RuntimeException("Flow not found"));
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Integer id) {
		repository.findById(id).map(flow -> {
			flow.setIsDeleted(true);
			return repository.save(flow);
		}).orElseThrow(() -> new RuntimeException("Flow not found"));
	}

	@GetMapping("/{id}")
	public Flow get(@PathVariable Integer id) {
		return repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Step not found"));
	}
}
