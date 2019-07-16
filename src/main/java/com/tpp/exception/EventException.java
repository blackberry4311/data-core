package com.tpp.exception;

/**
 * should be {@link BusinessException } ?? or another {@link Exception} extended
 */
public class EventException extends Exception {

	public EventException(String message) {
		super(message);
	}

	public EventException(String message, Throwable cause) {
		super(message, cause);
	}
}
