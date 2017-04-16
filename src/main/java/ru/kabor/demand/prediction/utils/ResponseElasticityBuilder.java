package ru.kabor.demand.prediction.utils;

import ru.kabor.demand.prediction.entity.RequestElasticityParameterSingle;
import ru.kabor.demand.prediction.entity.ResponseElasticity;
import ru.kabor.demand.prediction.entity.WhsArtTimeline;

/** It builds result of calculating elasticity */
public class ResponseElasticityBuilder {

	/** Build success response
	 * @param elasticityParameter
	 * @param whsArtTimeline
	 * @param bestModelFormula
	 * @param bestModelCoeff
	 * @param bestModelError
	 * @param isResultWithTimeMoments
	 * @return
	 */
	public static ResponseElasticity buildSuccessResponseElasticity(RequestElasticityParameterSingle elasticityParameter, WhsArtTimeline whsArtTimeline, String bestModelFormula,
			double[] bestModelCoeff, Double bestModelError, Boolean isResultWithTimeMoments) {
		ResponseElasticity result = new ResponseElasticity();
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

	/** Build result with error
	 * @param elasticityParameter
	 * @param whsArtTimeline
	 * @param errorMessage
	 * @return
	 */
	public static ResponseElasticity buildErrorResponseElasticity(RequestElasticityParameterSingle elasticityParameter, WhsArtTimeline whsArtTimeline, String errorMessage) {
		ResponseElasticity result = new ResponseElasticity();
		result.setTimeMoments(whsArtTimeline.getTimeMoments());
		result.setWhsId(elasticityParameter.getWhsId());
		result.setArtId(elasticityParameter.getArtId());
		result.setErrorMessage(errorMessage);
		result.setHasError(true);
		return result;
	}
}
