package ru.kabor.demand.prediction.utils;

import java.util.concurrent.Callable;

import ru.kabor.demand.prediction.entity.RequestForecastAndElasticityParameterSingle;
import ru.kabor.demand.prediction.entity.ResponseForecastAndElasticity;
import ru.kabor.demand.prediction.entity.WhsArtTimeline;
import ru.kabor.demand.prediction.r.RUtils;

/** For calculating forecast and elasticity in executor service */
public class MultithreadingForecastAndElasticityCallable implements Callable<ResponseForecastAndElasticity> {

	RequestForecastAndElasticityParameterSingle forecastAndElasticityParameters;
	WhsArtTimeline whsArtTimeline;
	RUtils rUtils;

	public MultithreadingForecastAndElasticityCallable() {
		super();
	}

	public MultithreadingForecastAndElasticityCallable(RequestForecastAndElasticityParameterSingle forecastAndElasticityParameters, WhsArtTimeline whsArtTimeline, RUtils rUtils) {
		super();
		this.forecastAndElasticityParameters = forecastAndElasticityParameters;
		this.whsArtTimeline = whsArtTimeline;
		this.rUtils = rUtils;
	}

	@Override
	public ResponseForecastAndElasticity call() throws Exception {
		rUtils.calculateWhsArtTimelineSlope(whsArtTimeline,forecastAndElasticityParameters.getRequestForecastParameter().getSmoothType());	//Calculate slope
		rUtils.calculateWhsArtTimelineTrendSeasonalAndRandom(whsArtTimeline);																//Calculate trand,elasticity and remainder
		ResponseForecastAndElasticity res = rUtils.makeForecastAndElasticity(forecastAndElasticityParameters, whsArtTimeline);				//Calculate forecast and elasticity
		return res;
	}

	public RequestForecastAndElasticityParameterSingle getForecastAndElasticityParameters() {
		return forecastAndElasticityParameters;
	}

	public void setForecastAndElasticityParameters(RequestForecastAndElasticityParameterSingle forecastAndElasticityParameters) {
		this.forecastAndElasticityParameters = forecastAndElasticityParameters;
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
