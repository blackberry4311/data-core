package com.tpp.persistence.util;

import com.tpp.exception.DaoException;
import org.hibernate.exception.DataException;

import java.io.Serializable;

public interface Lock {

	/**
	 * lock an object exclusively for being updated within a transaction
	 *
	 * @param type
	 * @param objectId
	 * @return
	 * @throws DaoException
	 */
	<T> T lockWrite(Class<T> type, Serializable objectId) throws DaoException;

	/**
	 * try to lock an object asynchronously
	 * @param type
	 * @param objectId
	 * @param timeoutMillis timeout in milliseconds
	 * @return an object if succeeded; otherwise, return null
	 * @throws XDataException
	 */
	//<T> XLockAsync<T> lockWriteAsync(Class<T> type, String objectId) throws DataException;

	/**
	 * release a locked object
	 *
	 * @param locked
	 * @throws DataException
	 */
	void release(Object locked) throws DaoException;
}
