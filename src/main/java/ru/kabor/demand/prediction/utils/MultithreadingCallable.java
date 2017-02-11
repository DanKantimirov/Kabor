package ru.kabor.demand.prediction.utils;

import java.util.concurrent.Callable;

import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponceForecast;
import ru.kabor.demand.prediction.entity.WhsArtTimeline;
import ru.kabor.demand.prediction.r.RUtils;

public class MultithreadingCallable implements Callable<ResponceForecast> {

	RequestForecastParameterSingle forecastParameters;
	WhsArtTimeline whsArtTimeline;
	RUtils rUtils;



	public MultithreadingCallable(RequestForecastParameterSingle forecastParameters, WhsArtTimeline whsArtTimeline,
			RUtils rUtils) {
		super();
		this.forecastParameters = forecastParameters;
		this.whsArtTimeline = whsArtTimeline;
		this.rUtils = rUtils;
	}

	@Override
	public ResponceForecast call() throws Exception {
		ResponceForecast res =  rUtils.makePrediction(forecastParameters, whsArtTimeline);
		//System.out.println(res);
		return res;
	}

	public RequestForecastParameterSingle getForecastParameters() {
		return forecastParameters;
	}

	public void setForecastParameters(RequestForecastParameterSingle forecastParameters) {
		this.forecastParameters = forecastParameters;
	}

	public WhsArtTimeline getWhsArtTimeline() {
		return whsArtTimeline;
	}

	public void setWhsArtTimeline(WhsArtTimeline whsArtTimeline) {
		this.whsArtTimeline = whsArtTimeline;
	}

	public RUtils getrUtils() {
		return rUtils;
	}

	public void setrUtils(RUtils rUtils) {
		this.rUtils = rUtils;
	}
	
	
}
