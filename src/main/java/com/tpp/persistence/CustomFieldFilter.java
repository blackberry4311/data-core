package com.tpp.persistence;

import org.hibernate.criterion.Criterion;

import java.util.Collection;

public interface CustomFieldFilter {
	/**
	 * Custom Criterion for a field
	 * 
	 * @param fieldName
	 * @param value
	 * @return null to use default Criterion from abstract class , return empty
	 *         list to ignore this field
	 */
	Collection<? extends Criterion> filter(String fieldName, Object value);
}
