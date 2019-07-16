package com.tpp.persistence;

import com.tpp.exception.DaoException;

import java.util.List;
import java.util.Map;

/**
 * Extract from project inventory, but remove refType , and refactor to hide complexity of using.
 * Simple see test SampleDao in in mc-persistence to understand
 */
public interface PropertyEntityDAO {

	/**
	 * Save or update a BaseProperty
	 *
	 * @param property
	 * @return
	 * @throws DaoException
	 */
	PropertyEntity save(String entityName, PropertyEntity property) throws DaoException;

	/**
	 * Save or update a list of BaseProperty
	 *
	 * @param propertyList
	 * @return
	 * @throws DaoException
	 */
	List<PropertyEntity> save(String entityName, List<PropertyEntity> propertyList) throws DaoException;

	/**
	 * Save or update a list of properties with ref type and ref id
	 *
	 * @param refType
	 * @param refId
	 * @param properties
	 * @return
	 * @throws DaoException
	 */
	List<PropertyEntity> save(String entityName, String refId, Map<String, Object> properties) throws DaoException;

	/**
	 * Remove xproperties of an object
	 *
	 * @param refId
	 * @return
	 * @throws DaoException
	 */
	int remove(String entityName, String refId) throws DaoException;

	/**
	 * Search xproperties of an object
	 *
	 * @param refId
	 * @param refType
	 * @return
	 * @throws DaoException
	 */
	List<PropertyEntity> searchBaseProperty(String entityName, String refId, boolean forUpdate) throws DaoException;

	/**
	 * Search xproperties of objects
	 *
	 * @param refIds
	 * @return
	 * @throws DaoException
	 */
	List<PropertyEntity> searchBaseProperty(String entityName, List<String> refIds, boolean forUpdate)
			throws DaoException;

	/**
	 * search xproperties from a with a known name
	 *
	 * @param name
	 * @return
	 * @throws DaoException
	 */
	List<PropertyEntity> searchBasePropertyByName(String entityName, String name, int page, int max)
			throws DaoException;
}
