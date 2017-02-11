package ru.kabor.demand.prediction.r;

public class RConnectionPoolException extends Exception {

	private static final long serialVersionUID = 2494494909137945514L;
	/** Error message */
	private String errorMessage;

	public String getErrorMessage() {
		return errorMessage;
	}

	public RConnectionPoolException(String errorMessage) {
		super();
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		return "RConnectionPoolException [=" + errorMessage + "]";
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
