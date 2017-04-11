package ru.kabor.demand.prediction.entity;

/**
 * It is element of time series. Contains current value, seasonal, trend and random
 * parts
 */
public class TimeSeriesElement {

	private Double actualValue;		//value from statistic
	private Double seasonalValue;	//seasonality
	private Double trendValue;		//trand
	private Double randomValue;		//random
	private Double smoothedValue;	//smooted value

	public TimeSeriesElement(Double actualValue) {
		super();
		this.actualValue = actualValue;
	}

	public TimeSeriesElement(Double actualValue, Double seasonalValue, Double trendValue, Double randomValue, Double smoothedValue) {
		super();
		this.actualValue = actualValue;
		this.seasonalValue = seasonalValue;
		this.trendValue = trendValue;
		this.randomValue = randomValue;
		this.smoothedValue = smoothedValue;
	}
	
	@Override
	public String toString() {
		return "TimeSeriesElement [actualValue=" + actualValue + ", seasonalValue=" + seasonalValue + ", trendValue=" + trendValue + ", randomValue=" + randomValue
				+ ", smoothedValue=" + smoothedValue + "]";
	}
	
	public Double getActualValue() {
		return actualValue;
	}

	public void setActualValue(Double actualValue) {
		this.actualValue = actualValue;
	}

	public Double getSeasonalValue() {
		return seasonalValue;
	}

	public void setSeasonalValue(Double seasonalValue) {
		this.seasonalValue = seasonalValue;
	}

	public Double getTrendValue() {
		return trendValue;
	}

	public void setTrendValue(Double trendValue) {
		this.trendValue = trendValue;
	}

	public Double getRandomValue() {
		return randomValue;
	}

	public void setRandomValue(Double randomValue) {
		this.randomValue = randomValue;
	}

	public Double getSmoothedValue() {
		return smoothedValue;
	}

	public void setSmoothedValue(Double smoothedValue) {
		this.smoothedValue = smoothedValue;
	}
}
