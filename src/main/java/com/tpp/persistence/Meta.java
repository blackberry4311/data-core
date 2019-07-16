package com.tpp.persistence;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class Meta extends LinkedHashMap<String, Object> implements Serializable {

	private static final long serialVersionUID = -8539228749033952622L;

	/**
	 * set extended property value
	 *
	 * @param property
	 * @param value
	 */
	public void setValue(String property, Object value) {
		if (value == null) remove(property);
		else put(property, value);
	}

	/**
	 * get a string value of a property
	 *
	 * @param property
	 * @return
	 */
	public String getStringValue(String property) {
		if (!containsKey(property)) return null;
		Object value = get(property);
		if (value == null) return null;

		if (value instanceof String) return (String) value;

		return value.toString();
	}

	/**
	 * return a property value
	 *
	 * @param property
	 * @return
	 * @since 0.2.0 allowed implicit cast
	 */
	public <T> T getValue(String property) {
		@SuppressWarnings("unchecked") T t = (T) get(property);
		return t;
	}

	/**
	 * Return an integer value of a property.
	 *
	 * @param property
	 * @return
	 */
	public Integer getIntValue(String property) {
		if (!containsKey(property)) return null;
		Object value = get(property);
		if (value == null) return null;
		if (value instanceof Number) return ((Number) value).intValue();
		if (value instanceof String) return Integer.parseInt((String) value);
		throw new RuntimeException(
				"Could not convert integer from unknown type: " + value.getClass().getCanonicalName());
	}

	/**
	 * Return a long value of a property.
	 *
	 * @param property
	 * @return
	 */
	public long getLongValue(String property) {
		if (!containsKey(property)) return 0;
		Object value = get(property);
		if (value == null) return 0;
		if (value instanceof Number) return ((Number) value).longValue();
		if (value instanceof String) return Long.parseLong((String) value);
		throw new RuntimeException("Could not convert long from unknown type: " + value.getClass().getCanonicalName());
	}

	/**
	 * Return a float value of a property.
	 *
	 * @param property
	 * @return
	 */
	public float getFloatValue(String property) {
		if (!containsKey(property)) return 0;
		Object value = get(property);
		if (value == null) return 0;
		if (value instanceof Number) return ((Number) value).floatValue();
		if (value instanceof String) return Float.parseFloat((String) value);
		throw new RuntimeException("Could not convert float from unknown type: " + value.getClass().getCanonicalName());
	}

	/**
	 * Return an double value of a property.
	 *
	 * @param property
	 * @return
	 */
	public double getDoubleValue(String property) {
		if (!containsKey(property)) return 0;
		Object value = get(property);
		if (value == null) return 0;
		if (value instanceof Number) return ((Number) value).doubleValue();
		if (value instanceof String) return Double.parseDouble((String) value);
		throw new RuntimeException("Could not convert doule from unknown type: " + value.getClass().getCanonicalName());
	}

	/**
	 * Return an integer value of a property.
	 *
	 * @param property
	 * @return
	 */
	public Date getDateValue(String property) {
		if (!containsKey(property)) return null;
		Object value = get(property);
		if (value == null) return null;
		if (value instanceof Date) return (Date) value;
		try {
			if (value instanceof String) return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse((String) value);
		} catch (ParseException ex) {
			throw new RuntimeException("Unable to parse date from " + value, ex);
		}
		throw new RuntimeException("Could not convert date from unknown type: " + value.getClass().getCanonicalName());
	}

	/**
	 * get list value of a property
	 *
	 * @param property
	 * @return
	 */
	public List<?> getListValue(String property) {
		if (!containsKey(property)) return null;
		Object value = get(property);
		if (value == null) return null;
		if (value instanceof List) return (List<?>) value;
		throw new RuntimeException("Could not convert list from unknown type: " + value.getClass().getCanonicalName());
	}

	/**
	 * get map value of a property
	 *
	 * @param property
	 * @return
	 */
	public Map<?, ?> getMapValue(String property) {
		if (!containsKey(property)) return null;
		Object value = get(property);
		if (value == null) return null;
		if (value instanceof Map) return (Map<?, ?>) value;
		throw new RuntimeException("Could not convert map from unknown type: " + value.getClass().getCanonicalName());
	}

	public boolean hasProperty(String property) {
		return containsKey(property);
	}
}
