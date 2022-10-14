package net.cattweasel.cropbytes.tools;

/**
 * Encapsulation for several exception types within implementation.
 * 
 * @author cattweasel
 *
 */
public class GeneralException extends Exception {

	private static final long serialVersionUID = -4255723435210902579L;

	/**
	 * Create a new exception based by a message.
	 * 
	 * @param message The message for this exception
	 */
	public GeneralException(String message) {
		super(message);
	}
	
	/**
	 * Create a new exception based by a parent exception.
	 * 
	 * @param exception The parent exception for this exception
	 */
	public GeneralException(Exception exception) {
		super(exception);
	}
}
