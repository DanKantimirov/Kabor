package ru.kabor.demand.prediction.utils.exceptions;

/** Exception when columns' headers in input file are incorrect */
public class InvalidHeaderException extends Exception {

	private static final long serialVersionUID = -4006717671066903447L;
	private String errorMessage = "";

	public InvalidHeaderException(String errorMessage) {
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
