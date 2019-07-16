package com.tpp.persistence;

import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 */
public abstract class AbstractExtensibleEntity<ID extends Serializable> extends AbstractEntity<ID> {

	/**
	 *
	 */
	private static final long serialVersionUID = -7408053693475130347L;

	protected String PROPERTY_ENTITY_NAME = null;

	public boolean isPropNotExisted() {
		if (StringUtils.isEmpty(PROPERTY_ENTITY_NAME)) {
			return true;
		}
		return false;
	}

	private Meta meta = new Meta();

	public Meta getMeta() {
		return meta;
	}

	public void setMeta(Meta meta) {
		if (meta != null) {
			this.meta.putAll(meta);
		} else {
			this.meta.clear();
		}
	}

//	public void setMeta(Map<String, Object> props) {
//		if (props != null) {
//			meta.putAll(props);
//		} else {
//			meta.clear();
//		}
//	}

	public void setValue(String property, Object value) {
		meta.setValue(property, value);
	}

	public void deleteValue(String property) {
		meta.put(property, null);
	}

	public String getStringValue(String property) {
		return meta.getStringValue(property);
	}

	public <T> T getValue(String property) {
		return meta.getValue(property);
	}

	public int getIntValue(String property) {
		return meta.getIntValue(property);
	}

	public long getLongValue(String property) {
		return meta.getLongValue(property);
	}

	public float getFloatValue(String property) {
		return meta.getFloatValue(property);
	}

	public double getDoubleValue(String property) {
		return meta.getDoubleValue(property);
	}

	public Date getDateValue(String property) {
		return meta.getDateValue(property);
	}

	public List<?> getListValue(String property) {
		return meta.getListValue(property);
	}

	public Map<?, ?> getMapValue(String property) {
		return meta.getMapValue(property);
	}

	public boolean hasProperty(String property) {
		return meta.containsKey(property);
	}

}
