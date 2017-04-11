package ru.kabor.demand.prediction.service;

/** Exception for DataService */
public class DataServiceException extends Exception {

	private static final long serialVersionUID = 7459092831960676456L;
	private String errorMessage = "";

	public DataServiceException(String errorMessage) {
		super();
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		return "DataServiceException =" + errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
