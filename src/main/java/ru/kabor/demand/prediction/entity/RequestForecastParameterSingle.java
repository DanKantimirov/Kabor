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
public class RequestForecastParameterSingle implements Serializable {

	@Transient
	private static final long serialVersionUID = -7649764838904201023L;
	@XmlElement
	private Integer requestId;
	@XmlElement
	private Integer whsId;
	@XmlElement
	private Integer artId;
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

	public RequestForecastParameterSingle() {
		super();
	}

	public RequestForecastParameterSingle(Integer requestId, Integer whsId, Integer artId, String trainingStartDate,
			String trainingEndDate, Integer forecastDuration, FORECAST_METHOD forecastMethod, SMOOTH_TYPE smoothType) {
		super();
		this.requestId = requestId;
		this.whsId = whsId;
		this.artId = artId;
		this.trainingStart = trainingStartDate;
		this.trainingEnd = trainingEndDate;
		this.forecastDuration = forecastDuration;
		this.forecastMethod = forecastMethod;
		this.smoothType = smoothType;
	}

	@Override
	public String toString() {
		return "RequestForecastParameterSingle [requestId=" + requestId + ", whsId=" + whsId + ", artId=" + artId
				+ ", trainingStart=" + trainingStart + ", trainingEnd=" + trainingEnd + ", forecastDuration="
				+ forecastDuration + ", forecastMethod=" + forecastMethod + ", smoothType=" + smoothType + "]";
	}

	public Integer getWhsId() {
		return whsId;
	}

	public void setWhsId(Integer whsId) {
		this.whsId = whsId;
	}

	public Integer getArtId() {
		return artId;
	}

	public void setArtId(Integer artId) {
		this.artId = artId;
	}

	public void setTrainingStartDate(String trainingStart) {
		this.trainingStart = trainingStart;
	}

	public void setTrainingEndDate(String trainingEnd) {
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

	public SMOOTH_TYPE getSmoothType() {
		return smoothType;
	}

	public void setSmoothType(SMOOTH_TYPE smoothType) {
		this.smoothType = smoothType;
	}

	public Integer getRequestId() {
		return requestId;
	}

	public void setRequestId(Integer requestId) {
		this.requestId = requestId;
	}

}
