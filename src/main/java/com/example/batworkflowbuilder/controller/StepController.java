package com.example.batworkflowbuilder.controller;

import com.example.batworkflowbuilder.dto.StepRequestDto;
import com.example.batworkflowbuilder.entity.Step;
import com.example.batworkflowbuilder.repository.StepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/step")
@CrossOrigin
public class StepController {

	@Autowired
	private StepRepository repository;

	@GetMapping
	public List<Step> getAll() {
		return repository.findAll();
	}

	// 登録
	@PostMapping
	public Step create(@RequestBody StepRequestDto dto) {
		Step step = new Step();
		step.setName(dto.getName());
		step.setBatPath(dto.getBatPath());
		step.setMemo(dto.getMemo());
		return repository.save(step);
	}

	// 更新
	@PutMapping("/{id}")
	public Step update(@PathVariable Integer id, @RequestBody StepRequestDto dto) {
		return repository.findById(id).map(step -> {
			step.setName(dto.getName());
			step.setBatPath(dto.getBatPath());
			step.setMemo(dto.getMemo());
			return repository.save(step);
		}).orElseThrow(() -> new RuntimeException("Step not found"));
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Integer id) {
		repository.deleteById(id);
	}

	@GetMapping("/{id}")
	public Step get(@PathVariable Integer id) {
		return repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Step not found"));
	}
}
