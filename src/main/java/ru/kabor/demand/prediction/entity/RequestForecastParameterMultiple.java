package ru.kabor.demand.prediction.entity;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ru.kabor.demand.prediction.utils.FORECAST_METHOD;
import ru.kabor.demand.prediction.utils.SMOOTH_TYPE;

@XmlRootElement
@XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
public class RequestForecastParameterMultiple implements Serializable {

	@Transient
	private static final long serialVersionUID = 77361074742816905L;

	@XmlElement
	private String whsIdBulk;
	@XmlElement
	private String artBulk;
	@XmlElement
	private String trainingStart;
	@XmlElement
	private String trainingEnd;
	@XmlElement
	private Integer forecastDuration;
	@XmlElement
	private FORECAST_METHOD forecastMethod;
	@XmlElement
	private SMOOTH_TYPE smoothType;

	@Transient
	public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MMM-dd");

	public RequestForecastParameterMultiple() {
		super();
	}

	public RequestForecastParameterMultiple(String whsIdBulk, String artBulk, String trainingStart, String trainingEnd,
			Integer forecastDuration, FORECAST_METHOD forecastMethod, SMOOTH_TYPE smoothType) {
		super();
		this.whsIdBulk = whsIdBulk;
		this.artBulk = artBulk;
		this.trainingStart = trainingStart;
		this.trainingEnd = trainingEnd;
		this.forecastDuration = forecastDuration;
		this.forecastMethod = forecastMethod;
		this.smoothType = smoothType;
	}

	public String getWhsIdBulk() {
		return whsIdBulk;
	}

	public void setWhsIdBulk(String whsIdBulk) {
		this.whsIdBulk = whsIdBulk;
	}

	public String getArtBulk() {
		return artBulk;
	}

	public void setArtBulk(String artBulk) {
		this.artBulk = artBulk;
	}

	public String getTrainingStart() {
		return trainingStart;
	}

	public void setTrainingStart(String trainingStart) {
		this.trainingStart = trainingStart;
	}

	public String getTrainingEnd() {
		return trainingEnd;
	}

	public void setTrainingEnd(String trainingEnd) {
		this.trainingEnd = trainingEnd;
	}

	public Integer getForecastDuration() {
		return forecastDuration;
	}

	public void setForecastDuration(Integer forecastDuration) {
		this.forecastDuration = forecastDuration;
	}

	public FORECAST_METHOD getForecastMethod() {
		return forecastMethod;
	}

	public void setForecastMethod(FORECAST_METHOD forecastMethod) {
		this.forecastMethod = forecastMethod;
	}

	public SMOOTH_TYPE getSmoothType() {
		return smoothType;
	}

	public void setSmoothType(SMOOTH_TYPE smoothType) {
		this.smoothType = smoothType;
	}

	public static DateTimeFormatter getFormatter() {
		return formatter;
	}

	public static void setFormatter(DateTimeFormatter formatter) {
		RequestForecastParameterMultiple.formatter = formatter;
	}

	@Override
	public String toString() {
		return "RequestForecastParameterMultiple [whsIdBulk=" + whsIdBulk + ", artBulk=" + artBulk + ", trainingStart="
				+ trainingStart + ", trainingEnd=" + trainingEnd + ", forecastDuration=" + forecastDuration
				+ ", forecastMethod=" + forecastMethod + ", smoothType=" + smoothType + "]";
	}

}
