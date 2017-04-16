package ru.kabor.demand.prediction.entity;

/** That class describes result of making forecast and calculating elasticity for one shop and one product */
public class ResponseForecastAndElasticity {

	/** forecast result*/
	private ResponseForecast responseForecast;
	/** elasticity result*/
	private ResponseElasticity responseElasticity;

	public ResponseForecastAndElasticity() {
		super();
	}

	public ResponseForecastAndElasticity(ResponseForecast responseForecast, ResponseElasticity responseElasticity) {
		super();
		this.responseForecast = responseForecast;
		this.responseElasticity = responseElasticity;
	}

	@Override
	public String toString() {
		return "ResponseForecastAndElasticity [responseForecast=" + responseForecast + ", responseElasticity=" + responseElasticity + "]";
	}

	public ResponseForecast getResponseForecast() {
		return responseForecast;
	}

	public void setResponseForecast(ResponseForecast responseForecast) {
		this.responseForecast = responseForecast;
	}

	public ResponseElasticity getResponseElasticity() {
		return responseElasticity;
	}

	public void setResponseElasticity(ResponseElasticity responseElasticity) {
		this.responseElasticity = responseElasticity;
	}
}
