package ru.kabor.demand.prediction.email;

public class EmailSenderException extends Exception{

	private static final long serialVersionUID = -4655872661821666343L;
	private String errorMessage = "";
	
	public EmailSenderException(String errorMessage) {
		super();
		this.errorMessage = errorMessage;
	}
	
	@Override
	public String toString() {
		return "EmailSenderException=" + errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
