package com.tpp.pool;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ServicePool<T> implements BeanFactoryAware, InitializingBean, DisposableBean {

	private Logger logger = Logger.getLogger(ServicePool.class);

	private String beanId;
	private int initialInstances;
	private int maximumInstances;

	private ArrayList<T> lstPool;
	private ArrayList<T> lstAcquired;
	private BeanFactory beanFactory;

	private Semaphore lock;

	private ServiceConnectionManager<T> serviceConMgr;

	public ServicePool() {
		this(null, 0, 10);
	}

	public ServicePool(String beanId, int initial, int max) {
		setBeanId(beanId);
		setInitialInstances(initial);
		setMaximumInstances(max);
	}

	public ServicePool(int initial, int max) {
		setInitialInstances(initial);
		setMaximumInstances(max);
	}

	public T acquire() throws InterruptedException {
		return acquire(Long.MAX_VALUE);
	}

	/**
	 * Wait until the pool is available and acquire a service object from the pool.
	 *
	 * @return service object
	 * @throws InterruptedException
	 */
	public T acquire(long timeout) throws InterruptedException {
		T ret = null;
		Thread currentThread = Thread.currentThread();

		if (logger.isDebugEnabled()) logger.debug("Trying to ACQUIRE " + beanId + " for " + currentThread + " ...");

		if (lock != null) if (!lock.tryAcquire(timeout, TimeUnit.MILLISECONDS)) return null;

		try {
			ret = takeFromPool();
		} catch (Exception e) {
			if (lock != null) lock.release();
			throw new RuntimeException("Unable to connect to service", e);
		}
		return ret;
	}

	private synchronized T takeFromPool() throws Exception {
		Thread currentThread = Thread.currentThread();
		if ((maximumInstances <= 0 || (lstAcquired.size() < maximumInstances)) && lstPool.size() == 0) {
			lstPool.add(serviceConMgr.connectService());
		}
		int acquired = lstAcquired.size();
		if (logger.isDebugEnabled()) logger.debug("ACQUIRING " + beanId + "(" + acquired + ") for " + currentThread);
		T ret = lstPool.remove(0);
		if (logger.isDebugEnabled()) {
			if (lstAcquired.contains(ret)) throw new RuntimeException("This " + beanId + " has been acquired before");
		}
		lstAcquired.add(ret);
		if (logger.isDebugEnabled()) logger.debug(beanId + "(" + acquired + ") ACQUIRED for " + currentThread);

		return ret;
	}

	private synchronized void returnToPool(T service) {
		Thread currentThread = Thread.currentThread();
		int acquired = lstAcquired.size() - 1;
		if (logger.isDebugEnabled())
			logger.debug("RELEASING " + beanId + "(" + acquired + ")" + " for " + currentThread + " ...");
		boolean removed = lstAcquired.remove(service);
		if (!removed) throw new RuntimeException(beanId + " is not from this pool!");
		lstPool.add(service);
		if (logger.isDebugEnabled()) logger.debug(beanId + "(" + acquired + ")" + " RELEASED for " + currentThread);
	}

	/**
	 * Return a service object to the pool
	 *
	 * @param service
	 */
	public void release(T service) {
		Thread currentThread = Thread.currentThread();
		if (logger.isDebugEnabled()) logger.debug("Trying to RELEASE " + beanId + " for " + currentThread + " ...");
		this.returnToPool(service);
		if (lock != null) lock.release();
	}

	public void setServiceConnectionManager(ServiceConnectionManager<T> mgr) {
		serviceConMgr = mgr;
	}

	public String getBeanId() {
		return beanId;
	}

	public void setBeanId(String beanID) {
		this.beanId = beanID;
	}

	public int getInitialInstances() {
		return initialInstances;
	}

	public void setInitialInstances(int initialInstances) {
		if (lstPool != null) throw new IllegalStateException("pool is running");
		this.initialInstances = initialInstances;
	}

	public int getMaximumInstances() {
		return maximumInstances;
	}

	public void setMaximumInstances(int maxInstances) {
		if (lstPool != null) throw new IllegalStateException("pool is running");
		this.maximumInstances = maxInstances;
	}

	public int getAcquiredInstanceCount() {
		return lstAcquired.size();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void afterPropertiesSet() throws Exception {
		if (initialInstances < 0)
			throw new IllegalArgumentException("Invalid initialInstances: must be a non-negative interal.");
		if (beanFactory != null) {
			if (serviceConMgr != null) throw new IllegalArgumentException(
					"beanFactory and serviceConnectionManager must not be set together!");

			if (beanId == null) throw new IllegalArgumentException(
					"Invalid beanId: beanId must refer to a defined bean in application context.");

			serviceConMgr = new ServiceConnectionManager<T>() {
				@SuppressWarnings("unchecked")
				@Override
				public T connectService() throws Exception {
					T bean = (T) beanFactory.getBean(beanId);
					if (lstAcquired.contains(bean))
						throw new IllegalStateException(beanId + " is not scoped as prototype.");
					return bean;
				}

				@Override
				public void disconnectService(T service) throws Exception {
					if (beanFactory instanceof ConfigurableBeanFactory) {
						ConfigurableBeanFactory configBf = (ConfigurableBeanFactory) beanFactory;
						for (T bean : lstPool) {
							configBf.destroyBean(beanId, bean);
						}
					}
				}
			};
		} else if (serviceConMgr == null) {
			throw new IllegalArgumentException(
					"Invalid beanFactory and serviceConnectionManager, either of these value must be set!");
		} else {
			// serviceConnMgr is set
			logger.debug("serviceConMgr is set with " + serviceConMgr);
		}
		// creating lock
		if (maximumInstances > 0) {
			lock = new Semaphore(maximumInstances, false);
		} else {
			lock = null;
		}

		// create the pool
		lstPool = new ArrayList<T>(maximumInstances > 0 ? maximumInstances : 10);
		lstAcquired = new ArrayList<T>(maximumInstances > 0 ? maximumInstances : 10);

		// increase pool to initialInstances
		while (lstPool.size() < initialInstances) {
			lstPool.add(serviceConMgr.connectService());
		}

		logger.info("Initialized ServicePool of " + beanId + " with " + lstPool.size() + " initial instances.");
	}

	@Override
	public void destroy() throws Exception {
		for (T bean : lstPool) {
			serviceConMgr.disconnectService(bean);
		}
		logger.info("Destroying ServicePool of " + beanId + " with " + lstAcquired.size() + " instances occupied.");
		lstAcquired.clear();
		lstAcquired = null;

		lstPool.clear();
		lstPool = null;
	}

	@Override
	public void setBeanFactory(BeanFactory value) throws BeansException {
		beanFactory = value;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

}

