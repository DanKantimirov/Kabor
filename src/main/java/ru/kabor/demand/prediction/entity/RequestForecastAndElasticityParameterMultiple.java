package ru.kabor.demand.prediction.entity;

import java.io.Serializable;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * That class describes request for making forecast and calculating elasticity
 * together (many shops and many products)
 */
@XmlRootElement
@XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
public class RequestForecastAndElasticityParameterMultiple implements Serializable {

	@Transient
	private static final long serialVersionUID = 4763110033157377149L;
	@XmlElement
	private RequestForecastParameterMultiple requestForecastParameterMultiple;
	@XmlElement
	private RequestElasticityParameterMultiple requestElasticityParameterMultiple;

	public RequestForecastAndElasticityParameterMultiple() {
		super();
	}

	public RequestForecastAndElasticityParameterMultiple(RequestForecastParameterMultiple requestForecastParameterMultiple,
			RequestElasticityParameterMultiple requestElasticityParameterMultiple) {
		super();
		this.requestForecastParameterMultiple = requestForecastParameterMultiple;
		this.requestElasticityParameterMultiple = requestElasticityParameterMultiple;
	}

	public RequestForecastParameterMultiple getRequestForecastParameterMultiple() {
		return requestForecastParameterMultiple;
	}

	public void setRequestForecastParameterMultiple(RequestForecastParameterMultiple requestForecastParameterMultiple) {
		this.requestForecastParameterMultiple = requestForecastParameterMultiple;
	}

	public RequestElasticityParameterMultiple getRequestElasticityParameterMultiple() {
		return requestElasticityParameterMultiple;
	}

	public void setRequestElasticityParameterMultiple(RequestElasticityParameterMultiple requestElasticityParameterMultiple) {
		this.requestElasticityParameterMultiple = requestElasticityParameterMultiple;
	}

}
