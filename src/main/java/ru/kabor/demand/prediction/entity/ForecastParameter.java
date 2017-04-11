package ru.kabor.demand.prediction.entity;


import ru.kabor.demand.prediction.utils.FORECAST_METHOD;
import ru.kabor.demand.prediction.utils.SMOOTH_TYPE;

import javax.persistence.*;

/** It represents parameters for making forecast (from database) */
@Entity
@Table(name = "v_forecast_parameter")
public class ForecastParameter {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @OneToOne(optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;
    
    @Column(name = "duration", nullable = false)
    private int duration;
    
    @Column(name = "forecast_method", length = 150, nullable = false)
    @Enumerated(EnumType.STRING)
    private FORECAST_METHOD forecast_method;
    
    @Column(name = "smoothing_method", length = 150, nullable = false)
    @Enumerated(EnumType.STRING)
    private SMOOTH_TYPE smoothing_method;
    
    

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ForecastParameter other = (ForecastParameter) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public ForecastParameter() {
		super();
	}

	public ForecastParameter(int id, Request request, int duration, FORECAST_METHOD forecast_method,
			SMOOTH_TYPE smoothing_method) {
		super();
		this.id = id;
		this.request = request;
		this.duration = duration;
		this.forecast_method = forecast_method;
		this.smoothing_method = smoothing_method;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public FORECAST_METHOD getForecast_method() {
		return forecast_method;
	}

	public void setForecast_method(FORECAST_METHOD forecast_method) {
		this.forecast_method = forecast_method;
	}

	public SMOOTH_TYPE getSmoothing_method() {
		return smoothing_method;
	}

	public void setSmoothing_method(SMOOTH_TYPE smoothing_method) {
		this.smoothing_method = smoothing_method;
	}
}
