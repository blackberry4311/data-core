package com.tpp.exception;

public class DataAccessException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -2020704038423229074L;

	public DataAccessException() {
		super();
	}

	public DataAccessException(String msg) {
		super(msg);
	}

	public DataAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
