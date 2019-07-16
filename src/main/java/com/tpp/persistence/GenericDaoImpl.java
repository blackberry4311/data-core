package com.tpp.persistence;

import com.tpp.exception.DaoException;
import com.tpp.exception.DataAccessException;
import com.tpp.persistence.util.ByteArrayUtils;
import com.tpp.persistence.util.PropertyEntityUtil;
import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.type.*;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * transaction modification must be allowed by tech lead (MUST BE)
 */

@Transactional(value = "transactionManager", propagation = Propagation.SUPPORTS)
public class GenericDaoImpl implements GenericDao {

	protected HashMap<String, Class> relationship = new HashMap<>();

	public static final String[] supportedFormats = { "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss",
			"yyyy-MM-dd HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd" };
	protected Pattern leftOp = Pattern.compile("^([a-zA-Z_]+[a-zA-Z0-9_\\-]*)\\s*(>|>=|<|<=|!=|<>|=|~=)\\s*");

	private Logger logger = Logger.getLogger(GenericDaoImpl.class);

	private PropertyEntityDAO propertyDAO;

	public void setPropertyDAO(PropertyEntityDAO propertyDAO) {
		this.propertyDAO = propertyDAO;
	}

	private SessionFactory sessionFactory;

	// allow using custom SessionFactory when needed
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public GenericDaoImpl() {
		super();
	}

	public GenericDaoImpl(SessionFactory sessionFactory, PropertyEntityDAO propertyEntityDAO) {
		super();
		this.sessionFactory = sessionFactory;
		this.propertyDAO = propertyEntityDAO;
	}

	@Override
	@Transactional(value = "transactionManager", propagation = Propagation.MANDATORY)
	public <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> List<T> bulkCreate(List<T> entities) {
		for (T entity : entities) {
			create(entity);
		}
		return entities;
	}

	@Override
	public <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> long countAll(Class<T> clazz) {
		StringBuilder queryString = new StringBuilder();
		queryString.append("SELECT COUNT (*) FROM ").append(clazz.getName());

		Session session = getCurrentSession();
		Query query = session.createQuery(queryString.toString());
		Long result = (Long) query.uniqueResult();

		return result;
	}

	@Override
	@Transactional(value = "transactionManager", propagation = Propagation.MANDATORY)
	public <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> T create(T pdo) {
		// set created at
		if (pdo.getCreatedAt() == null) {
			// pdo.setCreatedAt(DateUtils.convertToUTCDate(new Date()));
			pdo.setCreatedAt(new Date()); // TODO: convert to UTC when FE also
			// converts other Dates to UTC
		}

		Serializable id = getCurrentSession().save(pdo);
		if (pdo.getId() == null) {
			pdo.setId((ID) id);
		}

		try {
			saveProperties(pdo);
		} catch (final Exception ex) {
			throw new HibernateException("failed to save properties", ex);
		}

		return pdo;
	}

	@Override
	@Transactional(value = "transactionManager", propagation = Propagation.MANDATORY)
	public <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> void delete(T pdo) {
		// delete all properties of items
		try {
			loadProperties(pdo);
		} catch (DaoException e) {
			logger.warn("cannot load properties of instance " + pdo);
		}

		for (Entry<String, Object> p : pdo.getMeta().entrySet()) {
			if (p.getKey().charAt(0) == '-') {
				continue;
			}
			p.setValue(null);
		}

		try {
			saveProperties(pdo);
		} catch (DaoException e) {
			logger.warn("cannot remove properties of instance " + pdo);
		}
		getCurrentSession().delete(pdo);
	}

	@Override
	public void flush() {
		// I really don't know why in some case, hibernate call update/insert
		// before this delete happend,
		// that why I try to flush this session when delete happened by this
		// function
		getCurrentSession().flush();
	}

	@Override
	public <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> boolean isExistent(Class<T> clazz, ID id) {
		if (StringUtils.isEmpty(id)) {
			return false;
		}

		T result = find(clazz, id);
		if (result == null) {
			return false;
		}

		return true;
	}

	@Override
	public <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> T find(Class<T> clazz, ID id) {
		return findForUpdate(clazz, id, false);
	}

	@Override
	public <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> T findForUpdate(Class<T> clazz, ID id,
			boolean forUpdate) {
		T result;
		if (forUpdate) {
			result = getCurrentSession().get(clazz, id, new LockOptions(LockMode.PESSIMISTIC_WRITE));
		} else {
			result = getCurrentSession().get(clazz, id);
		}

		return result;
	}

	@Override
	public <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> T findUnique(Class<T> clazz,
			String[] params, Object[] values) {
		StringBuilder queryString = new StringBuilder();
		queryString.append("FROM ").append(clazz.getName()).append(" WHERE ");

		for (String param : params) {
			queryString.append(param).append("=:").append(param).append(" AND ");
		}

		queryString.append(" 1 = 1");

		Session session = getCurrentSession();
		Query query = session.createQuery(queryString.toString());
		for (int i = 0; i < params.length; i++) {
			query.setParameter(params[i], values[i]);
		}
		T result = (T) query.uniqueResult();

		return result;
	}

	@Override
	public <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> List<T> list(Class<T> clazz,
			String[] params, Object[] values, String sortName, boolean asc, int offset, int limit) {
		List<Criterion> criterionList = toCriterionList(clazz, params, values);
		//create sort list
		LinkedHashSet<SearchCriteria.OrderBy> ordersBy = new LinkedHashSet<>();
		SearchCriteria.OrderBy orderBy = new SearchCriteria.OrderBy();
		orderBy.setSortName(sortName);
		orderBy.isSortAsc(asc);
		ordersBy.add(orderBy);
		return find(clazz, criterionList, null, ordersBy, offset, limit, false).getList();
	}

	@Override
	public <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> SearchResult<T> find(Class<T> clazz,
			SearchCriteria<T> searchCriteria) {
		return find(clazz, searchCriteria, searchCriteria.getCustomFieldFilter());
	}

	@Override
	@Transactional(value = "transactionManager", propagation = Propagation.MANDATORY)
	public <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> T update(T pdo) {
		pdo.setUpdatedAt(new Date());
		try {
			saveProperties(pdo);
		} catch (final Exception e) {
			throw new HibernateException("failed to update properties", e);
		}
		getCurrentSession().update(pdo);

		return pdo;
	}

	@Override
	@Transactional(value = "transactionManager", propagation = Propagation.MANDATORY)
	public <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> List<T> update(List<T> pdos) {
		for (T instance : pdos) {
			update(instance);
		}
		return pdos;
	}

	// Utilities //
	protected <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> Object autoBoxValue(Class<T> clazz,
			String field, Object value) {
		Object v = value;
		try {
			// Validate mapped fields
			Type type = getSessionFactory().getClassMetadata(clazz).getPropertyType(field);
			// for anyway we should cast for their mistake
			if (value != null && value instanceof Double) {
				Double doubleValue = (Double) value;
				if (type instanceof LongType) {
					v = doubleValue.longValue();
				} else if (type instanceof IntegerType) {
					v = doubleValue.intValue();
				}
			} else if (type instanceof TimestampType || type instanceof DateType) {
				if (value != null && value instanceof String) {

					for (String format : supportedFormats) {
						DateFormat df = new SimpleDateFormat(format);
						try {
							v = df.parse(value.toString());
							break;
						} catch (ParseException e) {
							if (logger.isTraceEnabled()) {
								logger.trace(e.getMessage(), e);
							}
						}
					}
				}

			}
			return v;
		} catch (QueryException e) {
			throw new IllegalArgumentException(ErrorCode.UNSUPPORTED_PARAM.value(field), e);
		}
	}

	protected <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> SearchResult<T> find(Class<T> clazz,
			final SearchCriteria<T> searchCriteria, CustomFieldFilter customFieldFilter) {
		T t = searchCriteria.getCriteria();

		final T sample = t;
		if (sample != null) {
			ReflectionUtils.doWithFields(clazz, (field) -> {
				try {
					// check unsupported mapped field
					getSessionFactory().getClassMetadata(clazz).getPropertyType(field.getName());

					// get value of field
					Object v = field.get(sample);
					// TODO: simplify this condition using "return"
					if ((v != null && !(v instanceof HashSet) || (v instanceof HashSet && ((HashSet) v).size() != 0))) {
						searchCriteria.getMeta().setValue(field.getName(), v);
					}
				} catch (QueryException e) {
					// ignore for not mapping this property in hibernate
				}
			}, (field) -> {
				if (!(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()))) {
					if (!field.isAccessible()) {
						ReflectionUtils.makeAccessible(field);
					}
				} else {
					return false;
				}

				return !Meta.class.isAssignableFrom(field.getType());
			});
		}

		List<Criterion> criteria = new ArrayList<Criterion>();
		for (Entry<String, Object> entry : searchCriteria.getMeta().entrySet()) {
			if (entry.getKey().matches("^[a-zA-Z_]+.*$")) {
				Collection<? extends Criterion> criterion = null;
				if (customFieldFilter != null
						&& (criterion = customFieldFilter.filter(entry.getKey(), entry.getValue())) != null) {
					// TODO: implement customFieldFilter
					criteria.addAll(criterion);
				} else {
					criteria.add(toCriterion(clazz, null, entry.getKey(), entry.getValue()));
				}
			}
		}

		//map all join criteria to current criteria
		List<JoinCriteria> joins = searchCriteria.getJoinCriteria();
		Map<String, JoinType> joinAlias = null;
		if (joins != null && joins.size() > 0) {
			joinAlias = new HashMap<>();
			for (JoinCriteria join : joins) {
				joinAlias.putAll(toJoinCriteria(join.getJoinName(), join.getJoinType()));

				for (Entry<String, Object> entry : join.getCriteria().entrySet()) {
					criteria.add(toCriterion(toFinalJoinClass(join.getJoinName()), toFinalJoinName(join.getJoinName()),
							entry.getKey(), entry.getValue()));
				}
			}
		}

		LinkedHashSet<SearchCriteria.OrderBy> finalOrders = new LinkedHashSet<>();
		if (searchCriteria.getSortName() != null && searchCriteria.getSortName().trim() != "") {
			SearchCriteria.OrderBy orderBy = new SearchCriteria.OrderBy();
			orderBy.setSortName(searchCriteria.getSortName());
			orderBy.isSortAsc(searchCriteria.isSortAsc());
			finalOrders.add(orderBy);
		}
		finalOrders.addAll(searchCriteria.getOrdersBy());

		return find(clazz, criteria, joinAlias, finalOrders, searchCriteria.getPageIndex(),
				searchCriteria.getPageSize(), true);
	}

	/**
	 * this function used to convert pathParam to join param
	 * such as: studentProfile.demands.demand -> studentProfile, studentProfile.demands, demands.demand
	 *
	 * @param pathParam
	 * @param joinType
	 * @return
	 */
	protected Map<String, JoinType> toJoinCriteria(String pathParam, JoinType joinType) {
		Map<String, JoinType> join = new HashMap<>();
		String[] params = pathParam.split("\\.");

		for (int i = 0; i < params.length; i++) {
			if (relationship.get(params[i]) == null)
				throw new IllegalArgumentException(ErrorCode.UNSUPPORTED_JOIN_PARAM.value(params[i]));
			if (i == 0) {
				join.put(params[i], joinType);
			}
			if (i + 1 != params.length) {
				join.put(String.join(".", params[i], params[i + 1]), joinType);
			}
		}

		return join;
	}

	/**
	 * get finalJoinName to make sure no wrong alias
	 *
	 * @param pathParam
	 * @return
	 */
	protected String toFinalJoinName(String pathParam) {
		String[] params = pathParam.split("\\.");
		return params[params.length - 1];
	}

	/**
	 * get final join class support from relationship
	 *
	 * @param pathParam
	 * @return
	 */
	protected Class toFinalJoinClass(String pathParam) {
		String[] params = pathParam.split("\\.");
		Class clazz = relationship.get(params[params.length - 1]);
		if (clazz == null) throw new IllegalArgumentException(
				ErrorCode.UNSUPPORTED_JOIN_PARAM.value(params[params.length - 1] + "in pathParam: " + pathParam));
		return clazz;
	}

	protected <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> SearchResult<T> find(Class<T> clazz,
			List<Criterion> criteria, Map<String, JoinType> joinAliases, LinkedHashSet<SearchCriteria.OrderBy> ordersBy,
			int pageIndex, int pageSize, boolean isCountTotal) {
		if (pageSize < 1 || pageSize > SearchCriteria.MAX_PAGE_SIZE) {
			throw new DataAccessException("Invalid limit page size (" + pageSize + ")");
		}

		// build search query and count query
		Session session = getCurrentSession();
		Criteria query = session.createCriteria(clazz);
		Criteria rowCount = session.createCriteria(clazz);

		for (Criterion criterion : criteria) {
			query.add(criterion);
			rowCount.add(criterion);
		}

		//create join alias to query from relation table
		if (joinAliases != null && joinAliases.size() > 0) {
			for (Entry<String, JoinType> entry : joinAliases.entrySet()) {
				/*
				process beloew for split join string to alias only, such as: studentProfile.demands -> get demands as alias
				 */
				String[] _joinAlias = entry.getKey().split("\\.");
				String joinAlias = _joinAlias[_joinAlias.length - 1];
				//make alias query
				query.createAlias(entry.getKey(), joinAlias, entry.getValue());
				rowCount.createAlias(entry.getKey(), joinAlias, entry.getValue());
			}
		}

		rowCount.setProjection(Projections.rowCount());
		if (ordersBy.size() > 0) {
			for (SearchCriteria.OrderBy orderBy : ordersBy) {
				if (orderBy.getSortName().trim().equals("")) continue;
				query.addOrder(
						(orderBy.isSortAsc() ? Order.asc(orderBy.getSortName()) : Order.desc(orderBy.getSortName())));
			}
		}

		// paging
		int offset = (pageIndex - 1) * pageSize;
		if (pageSize > 0) {
			if (offset < 0) {
				offset = 0;
			}

			query.setFirstResult(offset).setMaxResults(pageSize);
		}

		int count = 0;
		if (isCountTotal) {
			List<Number> countResult = rowCount.list();
			if (countResult != null && !countResult.isEmpty()) {
				count = (countResult.get(0)).intValue();
			}
		}

		// do search
		List<T> list = query.list();

		// build search result
		SearchResult<T> searchResult = new SearchResult<>();
		searchResult.setPageIndex(offset);
		searchResult.setPageSize(pageSize);
		searchResult.setTotalRows(count);
		searchResult.buildPagingParams(pageIndex, pageSize, count);
		searchResult.setList(list);

		return searchResult;
	}

	protected Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	protected SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	protected <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> Criterion toCriterion(Class<T> clazz,
			String root /*root relationship */, String param, Object value) {
		String[] exp = toExpression(param);
		Object v = value;
		if (exp != null) {
			String field = exp[0];
			String operator = exp[1];

			v = autoBoxValue(clazz, field, value);
			if (root != null) field = String.join(".", root, field);

			if (">".equals(operator)) {
				return Restrictions.gt(field, v);
			} else if (">=".equals(operator)) {
				return Restrictions.ge(field, v);
			} else if ("<".equals(operator)) {
				return Restrictions.lt(field, v);
			} else if ("<=".equals(operator)) {
				return Restrictions.le(field, v);
			} else if ("=".equals(operator)) {
				return Restrictions.eq(field, v);
			} else if ("<>".equals(operator) || "!=".equals(operator)) {
				return Restrictions.neOrIsNotNull(field, v);
			} else if ("~=".equals(operator)) {
				return Restrictions.like(field, v);
			}
		}

		try {
			// Validate mapped fields
			getSessionFactory().getClassMetadata(clazz).getPropertyType(param);
		} catch (QueryException e) {
			throw new IllegalArgumentException(root != null ?
					ErrorCode.UNSUPPORTED_JOIN_PARAM.value(param) :
					ErrorCode.UNSUPPORTED_PARAM.value(param), e);
		}

		if (exp == null) {
			if (value instanceof Collection || value instanceof Object[]) {
				List<Object> list = new ArrayList<>();
				for (Object it : (Iterable<?>) value) {
					list.add(autoBoxValue(clazz, param, it));
				}
				v = list;
			}
		}

		if (root != null) {
			param = String.join(".", root, param);
		}

		if (value instanceof Object[]) {
			return Restrictions.in(param, (Object[]) v);
		} else if (value instanceof Collection) {
			return Restrictions.in(param, (Collection<?>) v);
		} else {
			return Restrictions.eqOrIsNull(param, v);
		}
	}

	protected <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> List<Criterion> toCriterionList(
			Class<T> clazz, String[] params, Object[] values) {
		ArrayList<Criterion> criterionList = new ArrayList<>();
		for (int i = 0; i < params.length; i++) {
			criterionList.add(toCriterion(clazz, null, params[i], values[i]));
		}
		return criterionList;
	}

	protected String[] toExpression(String s) {
		Matcher matcher = leftOp.matcher(s);
		if (matcher.matches()) {
			String fieldName = matcher.group(1);
			String op = matcher.group(2);
			return new String[] { fieldName, op };
		}

		return null;
	}

	// props support
	@Override
	public <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> boolean saveProperties(T object)
			throws DaoException {
		if (object == null || object.isPropNotExisted()) {
			return false;
		}
		String entityName = object.PROPERTY_ENTITY_NAME;

		// load properties
		@SuppressWarnings("unchecked")
		Map<String, PropertyEntity> map = (Map<String, PropertyEntity>) object.getValue("--properties");

		if (map == null) {
			map = loadMapProperties(object);
		}

		for (Entry<String, Object> mapEntry : object.getMeta().entrySet()) {
			String name = mapEntry.getKey();

			// ignore property begin with dash
			if (name.charAt(0) == '-') {
				continue;
			}

			// delete property with value is null
			PropertyEntity prop = map != null ? map.get(name) : null;
			if (mapEntry.getValue() == null) {
				if (prop != null) {
					// remove save prop
					getCurrentSession().delete(prop);
					map.remove(name);
				}
				continue;
			}

			// if it a new property, create new one
			PropertyEntity newprop = PropertyEntityUtil
					.createBaseProperty(String.valueOf(object.getId()), name, mapEntry.getValue());
			if (prop != null) {
				if (ByteArrayUtils.compare(prop.getValue(), newprop.getValue()) == 0) {
					continue; // value not changed
				}
				newprop.setId(prop.getId());
				newprop.setCreatedAt(prop.getCreatedAt());
			}
			propertyDAO.save(entityName, newprop);

			if (map == null) {
				map = new HashMap<>();
			}

			map.put(name, newprop);
		}

		return true;
	}

	@Override
	public <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> Map<String, PropertyEntity> loadProperties(
			T object) throws DaoException {

		if (object == null || object.isPropNotExisted() || object.getValue("--properties") != null) return null;

		ID targetId = object.getId();
		String entityName = object.PROPERTY_ENTITY_NAME;

		List<PropertyEntity> props = propertyDAO.searchBaseProperty(entityName, String.valueOf(targetId), false);

		if (props == null) return null;

		Map<String, PropertyEntity> map = new HashMap<>();

		for (PropertyEntity p : props) {
			map.put(p.getName(), p);
			object.setValue(p.getName(), PropertyEntityUtil.getValue(p));
		}

		// save properties
		object.setValue("--properties", map);
		return map;
	}

	private <ID extends Serializable, T extends AbstractExtensibleEntity<ID>> Map<String, PropertyEntity> loadMapProperties(
			T object) throws DaoException {

		if (object == null || object.isPropNotExisted() || object.getValue("--properties") != null) return null;

		ID targetId = object.getId();
		String entityName = object.PROPERTY_ENTITY_NAME;

		List<PropertyEntity> props = propertyDAO.searchBaseProperty(entityName, String.valueOf(targetId), false);

		if (props == null) return null;

		Map<String, PropertyEntity> map = new HashMap<>();

		for (PropertyEntity p : props) {
			map.put(p.getName(), p);
		}

		return map;
	}
}
