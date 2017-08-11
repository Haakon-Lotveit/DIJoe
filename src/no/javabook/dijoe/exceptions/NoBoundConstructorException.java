package no.javabook.dijoe.exceptions;

public class NoBoundConstructorException extends NoClassBoundException {

	/**
	 * automatic
	 */
	private static final long serialVersionUID = 1L;

	public NoBoundConstructorException(Class<?> type, String message) {
		super(type, message);
	}

	public NoBoundConstructorException(Class<?> type, Throwable cause) {
		super(type, cause);
	}

	public NoBoundConstructorException(Class<?> type, String message, Throwable cause) {
		super(type, message, cause);
	}

}
