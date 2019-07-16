package com.tpp.persistence;

import com.tpp.exception.DaoException;
import com.tpp.persistence.util.PropertyEntityUtil;
import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This should not be used alone, it will be compose with an entity
 * Somehow table have extend meta, should you this class
 *
 */
@Transactional(value = "transactionManager", propagation = Propagation.SUPPORTS)
public class PropertyEntityDAOImpl implements PropertyEntityDAO {
	private Logger logger = Logger.getLogger(PropertyEntityDAOImpl.class);

	private SessionFactory sessionFactory;

	public PropertyEntityDAOImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	@Transactional(value = "transactionManager", propagation = Propagation.MANDATORY)
	public PropertyEntity save(String entityName, PropertyEntity property) {
		if (property.getCreatedAt() == null) {
			property.setCreatedAt(new Date());
		}

		property.setUpdatedAt(new Date());
		getCurrentSession().merge(entityName, property);

		return property;

	}

	@Override
	@Transactional(value = "transactionManager", propagation = Propagation.MANDATORY)
	public List<PropertyEntity> save(String entityName, List<PropertyEntity> propertyList) throws DaoException {
		for (PropertyEntity p : propertyList) {
			save(entityName, p);
		}

		return propertyList;
	}

	@Override
	@Transactional(value = "transactionManager", propagation = Propagation.MANDATORY)
	public List<PropertyEntity> save(String entityName, String refId, Map<String, Object> properties)
			throws DaoException {
		// Convert a property map to property list then save it
		return save(entityName, toBasePropertyList(refId, properties));
	}

	@Override
	@Transactional(value = "transactionManager", propagation = Propagation.MANDATORY)
	public int remove(String entityName, String refId) throws DaoException {
		String sql = "Delete from " + entityName + " where ref_id=? ";
		Session session = getCurrentSession();
		Query queryObject = session.createQuery(sql);
		queryObject.setParameter("ref_id", refId);

		return queryObject.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PropertyEntity> searchBaseProperty(String entityName, String refId, boolean forUpdate)
			throws DaoException {
		Criteria criteria = createSpecificCriteria(entityName);
		if (criteria == null) return null;
		criteria.add(Restrictions.eq("refId", refId));
		criteria.setLockMode(forUpdate ? LockMode.PESSIMISTIC_WRITE : LockMode.NONE);
		List<PropertyEntity> items = criteria.list();

		return items;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PropertyEntity> searchBaseProperty(String entityName, List<String> refIds, boolean forUpdate)
			throws DaoException {
		Criteria criteria = createSpecificCriteria(entityName);
		if (criteria == null) return null;
		criteria.add(Restrictions.in("refId", refIds));
		criteria.setLockMode(forUpdate ? LockMode.PESSIMISTIC_WRITE : LockMode.NONE);
		List<PropertyEntity> items = criteria.list();

		return items;
	}

	/**
	 * Create specific instance of criteria according to refType
	 */
	private Criteria createSpecificCriteria(String entityName) {
		return getCurrentSession().createCriteria(entityName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PropertyEntity> searchBasePropertyByName(String entityName, String name, int page, int max)
			throws DaoException {
		Criteria criteria = createSpecificCriteria(entityName);
		criteria.add(Restrictions.eq("name", name));
		criteria.setFirstResult((page - 1) * max);
		criteria.setMaxResults(max);

		List<PropertyEntity> items = criteria.list();
		return items;
	}

	/**
	 * Convert a property map to xproperty list
	 */
	private List<PropertyEntity> toBasePropertyList(String refId, Map<String, Object> properties) {
		List<PropertyEntity> xpList = new ArrayList<PropertyEntity>();

		// Only add entry key without prefix '-'
		for (Entry<String, Object> entry : properties.entrySet()) {
			if (entry.getKey().charAt(0) != '-') {
				xpList.add(PropertyEntityUtil.createBaseProperty(refId, entry.getKey(), entry.getValue()));
			}
		}

		return xpList;
	}

	Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}
