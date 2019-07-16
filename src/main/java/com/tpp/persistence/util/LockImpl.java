package com.tpp.persistence.util;

import com.tpp.exception.DaoException;
import com.tpp.exception.DataNotFoundException;
import com.tpp.persistence.Meta;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

// extract from project inventory, not yet verified
public class LockImpl implements Lock {

	@Autowired
	SessionFactory sessionFactory;

	final Log LOG = LogFactory.getLog(LockImpl.class);

	private static final ThreadLocal<Map<Serializable, Object>> objLocks = new ThreadLocal<Map<Serializable, Object>>();

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T lockWrite(Class<T> type, Serializable objectId) throws DaoException {
		if (LOG.isTraceEnabled()) LOG.trace(
				"start locking W " + type.getSimpleName() + " " + objectId + " " + getCurrentSession()
						.getTransaction());
		T obj;
		try {
			obj = getLockedObject(type, objectId);
			if (obj == null) {
				obj = (T) getCurrentSession().get(type, objectId, new LockOptions(LockMode.PESSIMISTIC_WRITE));
				if (LOG.isDebugEnabled() && !LOG.isTraceEnabled()) LOG.debug(
						"locked W " + type.getSimpleName() + " " + objectId + " " + " " + getCurrentSession()
								.getTransaction());

				T xobj = obj;
				if (xobj instanceof Meta) {
					if (((Meta) xobj).getIntValue("-lockWrite") == 0
							&& ((Meta) xobj).getIntValue("-cacheHibernate") == 1) {
						getCurrentSession().refresh(obj);
					} else {
						((Meta) xobj).setValue("-cacheHibernate", null);
					}
				}

			} else {
			}
		} catch (Throwable ex) {
			LOG.error("failed to lock " + type.getSimpleName() + " " + objectId, ex);
			throw new DaoException("failed to lock object " + type.getSimpleName() + " " + objectId, ex);
		}

		if (obj == null) throw new DataNotFoundException(type + " with id " + objectId + " is not found");

		T x = obj;
		if (x instanceof Meta) {
			if (((Meta) x).getIntValue("-lockWrite") == 0) {
				((Meta) x).setValue("-lockWrite", 1);
			} else {
				if (LOG.isDebugEnabled()) LOG.debug("Object " + type.getName() + " has been locked " + objectId);
			}
		}

		if (LOG.isTraceEnabled()) LOG.trace(
				"finished locking W " + type.getSimpleName() + " " + objectId + " " + getCurrentSession()
						.getTransaction());

		return obj;
	}

	@SuppressWarnings("unchecked")
	private <T> T getLockedObject(Class<T> type, Serializable objectId) {
		T t = null;
		Map<Serializable, Object> map = objLocks.get();
		if (map != null) {
			String key = type.getName() + "-" + objectId;
			Object obj = map.get(key);
			if (obj != null) {
				t = (T) obj;
			}
		}
		return t;
	}

	// Functions process aspect
	public void processCommittedXTrans() {
		releaseLockObjects();
	}

	public void processRollbackedXTrans(Throwable ex) {
		releaseLockObjects();
	}

	public void processCommittedXTask() {
		releaseLockObjects();
	}

	public void processRollbackedXTask(Throwable ex) {
		releaseLockObjects();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	void releaseLockObjects() {
		Map<Serializable, Object> map = objLocks.get();
		objLocks.set(null);
		if (map == null) return;

		for (Entry<Serializable, Object> entry : map.entrySet()) {
			Object obj = entry.getValue();
			if (LOG.isTraceEnabled()) LOG.trace("firing LOCK RELEASE event for " + obj);
			if (obj instanceof Meta) {
				Meta xobj = (Meta) obj;
				List<String> keys = new ArrayList<String>(xobj.keySet());
				for (String string : keys) {
					if (string.startsWith("-") || string.startsWith(".")) {
						xobj.setValue(string, null);
					}
				}
			}

		}
	}

	@Override
	public void release(Object locked) throws DaoException {
		// do nothing
	}

	/**
	 * <p>
	 * Get the current session
	 * </P>
	 *
	 * @return the current session
	 */
	private Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
}
