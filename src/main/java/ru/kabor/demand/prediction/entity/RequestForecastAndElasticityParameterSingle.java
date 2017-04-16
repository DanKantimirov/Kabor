package ru.kabor.demand.prediction.entity;

import java.io.Serializable;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** That class describes request for making forecast and calculating elasticity together (one shop and one product)*/
@XmlRootElement
@XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
public class RequestForecastAndElasticityParameterSingle implements Serializable{
	@Transient
	private static final long serialVersionUID = -6278167932899372114L;
	@XmlElement
	private RequestForecastParameterSingle requestForecastParameter;
	@XmlElement
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
