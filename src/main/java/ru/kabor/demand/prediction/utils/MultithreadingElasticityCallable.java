package ru.kabor.demand.prediction.utils;

import java.util.concurrent.Callable;

import ru.kabor.demand.prediction.entity.RequestElasticityParameterSingle;
import ru.kabor.demand.prediction.entity.ResponceElasticity;
import ru.kabor.demand.prediction.entity.WhsArtTimeline;
import ru.kabor.demand.prediction.r.RUtils;

/** For calculating elasticity in executor service */
public class MultithreadingElasticityCallable implements Callable<ResponceElasticity> {

	RequestElasticityParameterSingle elasticityParameters;
	WhsArtTimeline whsArtTimeline;
	RUtils rUtils;

	public MultithreadingElasticityCallable() {
		super();
	}

	public MultithreadingElasticityCallable(RequestElasticityParameterSingle elasticityParameters, WhsArtTimeline whsArtTimeline, RUtils rUtils) {
		super();
		this.elasticityParameters = elasticityParameters;
		this.whsArtTimeline = whsArtTimeline;
		this.rUtils = rUtils;
	}

	@Override
	public ResponceElasticity call() throws Exception {
		rUtils.calculateWhsArtTimelineSlope(whsArtTimeline,SMOOTH_TYPE.NO);								//Calculate slope
		rUtils.calculateWhsArtTimelineTrendSeasonalAndRandom(whsArtTimeline);							//Calculate trand,elasticity and remainder
		ResponceElasticity res = rUtils.makeElasticity(elasticityParameters, whsArtTimeline, true);		//Calculate elasticity
		return res;
	}

	public RequestElasticityParameterSingle getElasticityParameters() {
		return elasticityParameters;
	}

	public void setElasticityParameters(RequestElasticityParameterSingle elasticityParameters) {
		this.elasticityParameters = elasticityParameters;
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
