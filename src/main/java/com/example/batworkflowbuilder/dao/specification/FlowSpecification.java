package com.example.batworkflowbuilder.dao.specification;

import com.example.batworkflowbuilder.entity.Flow;
import org.springframework.data.jpa.domain.Specification;

public class FlowSpecification {
	public static Specification<Flow> notLogicalDeleted() {
		return (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
	}
}
