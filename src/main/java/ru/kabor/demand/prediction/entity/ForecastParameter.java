package ru.kabor.demand.prediction.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "v_sales_rest")
public class ForecastParameter {
	
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;
    
    @Column(name = "duration", nullable = false)
    private int duration;
    
    @Column(name = "forecast_method", length = 150, nullable = false)
    private String forecast_method;
    
    @Column(name = "smoothing_method", length = 150, nullable = false)
    private String smoothing_method;

    //TODO: delete from commit. It's done because Eclipse doesn't understand lombok lib
    
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

	public String getForecast_method() {
		return forecast_method;
	}

	public void setForecast_method(String forecast_method) {
		this.forecast_method = forecast_method;
	}

	public String getSmoothing_method() {
		return smoothing_method;
	}

	public void setSmoothing_method(String smoothing_method) {
		this.smoothing_method = smoothing_method;
	}
    
}
