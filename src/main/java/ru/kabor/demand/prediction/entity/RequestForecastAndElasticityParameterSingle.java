package ru.kabor.demand.prediction.entity;


/** That class describes request for making forecast and calculating elasticity together*/
public class RequestForecastAndElasticityParameterSingle {
	
	private RequestForecastParameterSingle requestForecastParameter;
	private RequestElasticityParameterSingle requestElasticityParameter;

	public RequestForecastAndElasticityParameterSingle() {
		super();
	}
	
	@Override
	public String toString() {
		return "RequestForecastAndElasticityParameterSingle [requestForecastParameter=" + requestForecastParameter + ", requestElasticityParameter=" + requestElasticityParameter
				+ "]";
	}

	public RequestForecastAndElasticityParameterSingle(RequestForecastParameterSingle requestForecastParameter, RequestElasticityParameterSingle requestElasticityParameter) {
		super();
		this.requestForecastParameter = requestForecastParameter;
		this.requestElasticityParameter = requestElasticityParameter;
	}

	public RequestForecastParameterSingle getRequestForecastParameter() {
		return requestForecastParameter;
	}

	public void setRequestForecastParameter(RequestForecastParameterSingle requestForecastParameter) {
		this.requestForecastParameter = requestForecastParameter;
	}

	public RequestElasticityParameterSingle getRequestElasticityParameter() {
		return requestElasticityParameter;
	}

	public void setRequestElasticityParameter(RequestElasticityParameterSingle requestElasticityParameter) {
		this.requestElasticityParameter = requestElasticityParameter;
	}

}
