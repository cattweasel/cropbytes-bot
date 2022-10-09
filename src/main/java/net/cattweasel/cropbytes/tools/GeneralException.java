package net.cattweasel.cropbytes.tools;

public class GeneralException extends Exception {

	private static final long serialVersionUID = -4255723435210902579L;

	public GeneralException(String message) {
		super(message);
	}
	
	public GeneralException(Exception exception) {
		super(exception);
	}
}
