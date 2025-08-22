package com.example.batworkflowbuilder.repository;

import com.example.batworkflowbuilder.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Integer> {
}