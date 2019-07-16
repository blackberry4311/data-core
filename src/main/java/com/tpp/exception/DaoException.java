package com.tpp.exception;

public class DaoException extends DataAccessException {

	/**
	 *
	 */
	private static final long serialVersionUID = 5863179798307754306L;

	public DaoException() {
		super();
	}

	public DaoException(String msg) {
		super(msg);
	}

	public DaoException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
