package com.tpp.persistence.util;

import com.tpp.persistence.Meta;
import com.tpp.persistence.PropertyEntity;

import java.util.Date;
import java.util.List;
import java.util.Map;

public final class PropertyEntityUtil {

	// Supported Object Types
	public static final String OBJ_TYPE_INT = "integer";
	public static final String OBJ_TYPE_DOUBLE = "double";
	public static final String OBJ_TYPE_STRING = "string";
	public static final String OBJ_TYPE_DATE = "date";
	public static final String OBJ_TYPE_LIST = "list";
	public static final String OBJ_TYPE_MAP = "map";

	public static Object getValue(PropertyEntity property) {
		Object value = null;
		String objType = property.getValueType();
		byte[] objValue = property.getValue();
		if (objValue == null || objValue.length == 0) {
			return null;
		}

		if (objType.equalsIgnoreCase(OBJ_TYPE_INT)) {
			value = objValue.length == 4 ? ByteArray.toInt(objValue) : ByteArray.toLong(objValue);
		} else if (objType.equalsIgnoreCase(OBJ_TYPE_DOUBLE)) {
			value = ByteArray.toDouble(objValue);
		} else if (objType.equalsIgnoreCase(OBJ_TYPE_STRING)) {
			value = ByteArray.toString(objValue);
		} else if (objType.equalsIgnoreCase(OBJ_TYPE_DATE)) {
			value = ByteArray.toDate(objValue);
		} else if (objType.equalsIgnoreCase(OBJ_TYPE_LIST)) {
			value = ByteArray.toList(objValue);
		} else if (objType.equalsIgnoreCase(OBJ_TYPE_MAP)) {
			value = ByteArray.toMap(objValue);
		}

		return value;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static PropertyEntity createBaseProperty(String refId, String name, Object value) {
		byte[] objValue = null;
		String objType = null;

		if (value instanceof String) {
			objType = OBJ_TYPE_STRING;
			objValue = ByteArray.toByta((String) value);
		} else if (value instanceof Integer || value instanceof Long) {
			objType = OBJ_TYPE_INT;
			objValue = ByteArray.toByta(((Number) value).longValue());
		} else if (value instanceof Double) {
			objType = OBJ_TYPE_DOUBLE;
			objValue = ByteArray.toByta((Double) value);
		} else if (value instanceof Date) {
			objType = OBJ_TYPE_DATE;
			objValue = ByteArray.toByta((Date) value);
		} else if (value instanceof List<?>) {
			objType = OBJ_TYPE_LIST;
			objValue = ByteArray.toByta((List) value);
		} else if (value instanceof Map<?, ?>) {
			objType = OBJ_TYPE_MAP;
			objValue = ByteArray.toByta((Map) value);
		}

		// Create a xproperty
		PropertyEntity property = new PropertyEntity();

		property.setRefId(refId);

		property.setName(name);
		property.setValue(objValue);
		property.setValueType(objType);

		return property;
	}

	public static void copy(List<PropertyEntity> fromPropList, Meta toMeta) {
		copy(fromPropList, toMeta, false);
	}

	public static void copy(List<PropertyEntity> fromPropList, Meta toMeta, boolean isOnlyAvailableMetaKey) {
		for (PropertyEntity p : fromPropList) {
			if (isOnlyAvailableMetaKey) {
				if (toMeta.hasProperty(p.getName())) {
					toMeta.setValue(p.getName(), PropertyEntityUtil.getValue(p));
				}
				continue;
			}

			toMeta.setValue(p.getName(), PropertyEntityUtil.getValue(p));
		}
	}
}
