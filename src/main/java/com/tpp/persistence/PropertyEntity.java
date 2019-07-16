package com.tpp.persistence;

import java.util.Arrays;

@SuppressWarnings("serial")
public class PropertyEntity extends AbstractEntity<String> {

	private String name;
	private String refId;
	private String valueType;
	private byte[] value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRefId() {
		return refId;
	}

	public void setRefId(String refId) {
		this.refId = refId;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "PropertyEntity [refId=" + refId + ", valueType=" + valueType + ", name=" + name + ", value=" + Arrays
				.toString(value) + "]";
	}
}