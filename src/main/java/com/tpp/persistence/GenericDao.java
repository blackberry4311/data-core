package com.tpp.persistence;

import com.tpp.exception.DaoException;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface GenericDao {

	<ID extends Serializable, T extends AbstractExtensibleEntity<ID>> List<T> bulkCreate(List<T> entities);

	<ID extends Serializable, T extends AbstractExtensibleEntity<ID>> long countAll(Class<T> clazz);

	<ID extends Serializable, T extends AbstractExtensibleEntity<ID>> T create(T pdo);

	<ID extends Serializable, T extends AbstractExtensibleEntity<ID>> void delete(T pdo);

	void flush();

	<ID extends Serializable, T extends AbstractExtensibleEntity<ID>> boolean isExistent(Class<T> clazz, ID id);

	<ID extends Serializable, T extends AbstractExtensibleEntity<ID>> T find(Class<T> clazz, ID id);

	<ID extends Serializable, T extends AbstractExtensibleEntity<ID>> T findForUpdate(Class<T> clazz, ID id,
			boolean forUpdate);

	// TODO: use map for params-values
	<ID extends Serializable, T extends AbstractExtensibleEntity<ID>> T findUnique(Class<T> clazz, String[] params,
			Object[] values);

	// TODO: use map for params-values
	<ID extends Serializable, T extends AbstractExtensibleEntity<ID>> List<T> list(Class<T> clazz, String[] params,
			Object[] values, String orderBy, boolean asc, int offset, int limit);

	<ID extends Serializable, T extends AbstractExtensibleEntity<ID>> SearchResult<T> find(Class<T> clazz,
			SearchCriteria<T> searchCriteria);

	<ID extends Serializable, T extends AbstractExtensibleEntity<ID>> T update(T pdo);

	<ID extends Serializable, T extends AbstractExtensibleEntity<ID>> List<T> update(List<T> pdos);

	<ID extends Serializable, T extends AbstractExtensibleEntity<ID>> Map<String, PropertyEntity> loadProperties(
			T object) throws DaoException;

	<ID extends Serializable, T extends AbstractExtensibleEntity<ID>> boolean saveProperties(T object)
			throws DaoException;
}

