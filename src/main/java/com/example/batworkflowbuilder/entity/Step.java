package com.example.batworkflowbuilder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "STEP")
@Getter
@Setter
public class Step {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_STEP")
	@SequenceGenerator(name = "SEQ_STEP", sequenceName = "SEQ_STEP", allocationSize = 1)
	private Integer id;

	@Column(name = "NAME", nullable = false, length = 100)
	private String name;

	@Column(name = "BAT_PATH", nullable = false, length = 512)
	private String batPath;

	@Column(length = 1000)
	private String memo;

	@Column(name = "is_deleted")
	private boolean isDeleted = false;
}
