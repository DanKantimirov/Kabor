package ru.kabor.demand.prediction.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;


public class TimeMomentDescription {
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate timeMoment;
	private Double salesQnty;
	@JsonIgnore
	private Double restQnty;

	public TimeMomentDescription() {
		super();
	}

	public TimeMomentDescription(LocalDate timeMoment, Double salesQnty, Double restQnty) {
		super();
		this.timeMoment = timeMoment;
		this.salesQnty = salesQnty;
		this.restQnty = restQnty;
	}

	public LocalDate getTimeMoment() {
		return timeMoment;
	}

	public void setTimeMoment(LocalDate timeMoment) {
		this.timeMoment = timeMoment;
	}

	public Double getSalesQnty() {
		return salesQnty;
	}

	public void setSalesQnty(Double salesQnty) {
		this.salesQnty = salesQnty;
	}

	public Double getRestQnty() {
		return restQnty;
	}

	public void setRestQnty(Double restQnty) {
		this.restQnty = restQnty;
	}

	@Override
	public String toString() {
		return "TimeMomentDescription [timeMoment=" + timeMoment + ", salesQnty=" + salesQnty + ", restQnty=" + restQnty
				+ "]";
	}
}
