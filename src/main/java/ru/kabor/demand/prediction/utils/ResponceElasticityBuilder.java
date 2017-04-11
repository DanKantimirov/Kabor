package ru.kabor.demand.prediction.utils;

import ru.kabor.demand.prediction.entity.RequestElasticityParameterSingle;
import ru.kabor.demand.prediction.entity.ResponceElasticity;
import ru.kabor.demand.prediction.entity.WhsArtTimeline;

/** It builds result of calculating elasticity */
public class ResponceElasticityBuilder {

	public static ResponceElasticity buildSuccessResponseElasticity(RequestElasticityParameterSingle elasticityParameter, WhsArtTimeline whsArtTimeline, String bestModelFormula,
			double[] bestModelCoeff, Double bestModelError, Boolean isResultWithTimeMoments) {
		ResponceElasticity result = new ResponceElasticity();
		if (isResultWithTimeMoments) {
			result.setTimeMoments(whsArtTimeline.getTimeMoments());
		}
		result.setWhsId(elasticityParameter.getWhsId());
		result.setArtId(elasticityParameter.getArtId());
		result.setFormula(bestModelFormula);
		result.setFunctionParameters(bestModelCoeff);
		result.setSigma(bestModelError);
		return result;
	}

	public static ResponceElasticity buildErrorResponseElasticity(RequestElasticityParameterSingle elasticityParameter, WhsArtTimeline whsArtTimeline, String errorMessage) {
		ResponceElasticity result = new ResponceElasticity();
		result.setTimeMoments(whsArtTimeline.getTimeMoments());
		result.setWhsId(elasticityParameter.getWhsId());
		result.setArtId(elasticityParameter.getArtId());
		result.setErrorMessage(errorMessage);
		result.setHasError(true);
		return result;
	}
}
