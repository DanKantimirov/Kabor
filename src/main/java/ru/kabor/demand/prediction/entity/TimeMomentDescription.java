package ru.kabor.demand.prediction.entity;

import java.time.LocalDate;

/** It represents information about sales, rests and price in particular moment of time */
public class TimeMomentDescription {

	private LocalDate timeMoment;
	private TimeSeriesElement sales;	//sales with trand, seasonality and random
	private TimeSeriesElement rest;		//rests with trand, seasonality and random
	private Double priceQnty = 0.0;

	public TimeMomentDescription() {
		super();
	}

	public TimeMomentDescription(LocalDate timeMoment, TimeSeriesElement sales, TimeSeriesElement rest) {
		super();
		this.timeMoment = timeMoment;
		this.sales = sales;
		this.rest = rest;
	}

	public TimeMomentDescription(LocalDate timeMoment, TimeSeriesElement sales, TimeSeriesElement rest, Double priceQnty) {
		super();
		this.timeMoment = timeMoment;
		this.sales = sales;
		this.rest = rest;
		this.priceQnty = priceQnty;
	}
	
	@Override
	public String toString() {
		return "TimeMomentDescription [timeMoment=" + timeMoment + ", salesQnty=" + this.sales.getActualValue() + ", restQnty=" + this.rest.getActualValue() + "]";
	}

	public LocalDate getTimeMoment() {
		return timeMoment;
	}

	public void setTimeMoment(LocalDate timeMoment) {
		this.timeMoment = timeMoment;
	}

	public TimeSeriesElement getSales() {
		return sales;
	}

	public void setSales(TimeSeriesElement sales) {
		this.sales = sales;
	}

	public TimeSeriesElement getRest() {
		return rest;
	}

	public void setRest(TimeSeriesElement rest) {
		this.rest = rest;
	}

	public Double getPriceQnty() {
		return priceQnty;
	}

	public void setPriceQnty(Double priceQnty) {
		this.priceQnty = priceQnty;
	}
}
