package no.javabook.dijoe.exceptions;

public class NoClassBoundException extends DIException {

	/**
	 * autogenerated
	 */
	private static final long serialVersionUID = 2652884915712345728L;

	public NoClassBoundException(Class<?> requestedClass) {
		super(requestedClass, generateMessage(requestedClass));
	}
	public NoClassBoundException(Class<?> requestedClass, String message) {
		super(requestedClass, message);
	}

	public NoClassBoundException(Class<?> requestedClass, Throwable cause) {
		super(requestedClass, generateMessage(requestedClass), cause);
	}

	public NoClassBoundException(Class<?> requestedClass, String message, Throwable cause) {
		super(requestedClass, message, cause);
	}
	
	private static final String generateMessage(Class<?> requestedClass) {
		return String.format("No class bound for requested class <%s>", requestedClass.getCanonicalName());
	}
	
}
