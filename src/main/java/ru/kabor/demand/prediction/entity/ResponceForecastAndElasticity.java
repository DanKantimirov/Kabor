package ru.kabor.demand.prediction.entity;

/** That class describes result of making forecast and calculating elasticity for one shop and one product */
public class ResponceForecastAndElasticity {

	private ResponceForecast responceForecast;			//forecast result
	private ResponceElasticity responceElasticity;		//elasticity result

	public ResponceForecastAndElasticity() {
		super();
	}

	public ResponceForecastAndElasticity(ResponceForecast responceForecast, ResponceElasticity responceElasticity) {
		super();
		this.responceForecast = responceForecast;
		this.responceElasticity = responceElasticity;
	}

	@Override
	public String toString() {
		return "ResponceForecastAndElasticity [responceForecast=" + responceForecast + ", responceElasticity=" + responceElasticity + "]";
	}

	public ResponceForecast getResponceForecast() {
		return responceForecast;
	}

	public void setResponceForecast(ResponceForecast responceForecast) {
		this.responceForecast = responceForecast;
	}

	public ResponceElasticity getResponceElasticity() {
		return responceElasticity;
	}

	public void setResponceElasticity(ResponceElasticity responceElasticity) {
		this.responceElasticity = responceElasticity;
	}
}
