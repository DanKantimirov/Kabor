package ru.kabor.demand.prediction.utils;

import java.util.concurrent.Callable;

import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponceForecast;
import ru.kabor.demand.prediction.entity.WhsArtTimeline;
import ru.kabor.demand.prediction.r.RUtils;

/** For making forecast in executor service */
public class MultithreadingForecastCallable implements Callable<ResponceForecast> {

	RequestForecastParameterSingle forecastParameters;
	WhsArtTimeline whsArtTimeline;
	RUtils rUtils;

	public MultithreadingForecastCallable(RequestForecastParameterSingle forecastParameters, WhsArtTimeline whsArtTimeline,
			RUtils rUtils) {
		super();
		this.forecastParameters = forecastParameters;
		this.whsArtTimeline = whsArtTimeline;
		this.rUtils = rUtils;
	}

	@Override
	public ResponceForecast call() throws Exception {
		rUtils.calculateWhsArtTimelineSlope(whsArtTimeline,forecastParameters.getSmoothType());			//calculate slope
		rUtils.calculateWhsArtTimelineTrendSeasonalAndRandom(whsArtTimeline);							//calculate trand,elasticity and remainder
		ResponceForecast res =  rUtils.makeForecast(forecastParameters, whsArtTimeline);
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
