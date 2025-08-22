package com.example.batworkflowbuilder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "POST")
@Getter
@Setter
public class Post {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_POST")
	@SequenceGenerator(name = "SEQ_POST", sequenceName = "SEQ_POST", allocationSize = 1)
	private Integer id;

	@Column(name = "TITLE", nullable = false, length = 100)
	private String title;

	@JdbcTypeCode(SqlTypes.LONGNVARCHAR)
	@Column(name = "CONTENT", columnDefinition = "NVARCHAR(MAX)", nullable = false)
	private String content;

	@UpdateTimestamp
	@Column(name = "UPDATED_AT")
	private LocalDateTime updatedAt;
}
