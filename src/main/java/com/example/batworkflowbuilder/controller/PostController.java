package com.example.batworkflowbuilder.controller;

import com.example.batworkflowbuilder.dto.PostRequestDto;
import com.example.batworkflowbuilder.entity.Post;
import com.example.batworkflowbuilder.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/post")
@CrossOrigin
public class PostController {

	@Autowired
	private PostRepository repository;

	@GetMapping
	public List<Post> getAll() {
		return repository.findAll();
	}

	@PostMapping
	public Post create(@RequestBody PostRequestDto dto) {
		Post post = new Post();
		post.setTitle(dto.getTitle());
		post.setContent(dto.getContent());
		post.setUpdatedAt(LocalDateTime.now());
		return repository.save(post);
	}

	@PutMapping("/{id}")
	public Post update(@PathVariable Integer id, @RequestBody PostRequestDto dto) {
		return repository.findById(id).map(post -> {
			post.setTitle(dto.getTitle());
			post.setContent(dto.getContent());
			post.setUpdatedAt(LocalDateTime.now());
			return repository.save(post);
		}).orElseThrow(() -> new RuntimeException("Post not found"));
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Integer id) {
		repository.deleteById(id);
	}

	@GetMapping("/{id}")
	public Post get(@PathVariable Integer id) {
		return repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Step not found"));
	}
}
