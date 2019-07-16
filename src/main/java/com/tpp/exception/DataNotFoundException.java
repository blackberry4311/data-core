package com.tpp.exception;

@SuppressWarnings("serial")
public class DataNotFoundException extends DaoException {

	public DataNotFoundException() {
		super();
	}

	public DataNotFoundException(String msg) {
		super(msg);
	}

	public DataNotFoundException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
