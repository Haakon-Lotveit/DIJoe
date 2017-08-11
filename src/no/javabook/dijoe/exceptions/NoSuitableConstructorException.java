package no.javabook.dijoe.exceptions;

public class NoSuitableConstructorException extends DIException {

	/**
	 * automatic
	 */
	private static final long serialVersionUID = -1741872605468680385L;

	public NoSuitableConstructorException(Class<?> requestedClass, String message) {
		super(requestedClass, message);
	}

	public NoSuitableConstructorException(Class<?> requestedClass, Throwable cause) {
		super(requestedClass, cause);
	}

	public NoSuitableConstructorException(Class<?> requestedClass, String message, Throwable cause) {
		super(requestedClass, message, cause);
	}

}
