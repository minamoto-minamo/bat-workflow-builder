package com.example.batworkflowbuilder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "WORKFLOW")
@Getter
@Setter
public class Flow {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_WORKFLOW")
	@SequenceGenerator(name = "SEQ_WORKFLOW", sequenceName = "SEQ_WORKFLOW", allocationSize = 1)
	private Integer id;

	@Column(name = "NAME", nullable = false, length = 100)
	private String name;

	@Column(name = "MEMO", length = 1000)
	private String memo;

	@JdbcTypeCode(SqlTypes.LONGNVARCHAR)
	@Column(name = "FLOW_JSON", columnDefinition = "NVARCHAR(MAX)", nullable = false)
	private String flowJson;

	@Column(name = "IS_DELETED")
	private Boolean isDeleted = false;

	@CreationTimestamp
	@Column(name = "CREATED_AT", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "UPDATED_AT")
	private LocalDateTime updatedAt;
}
