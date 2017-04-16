package ru.kabor.demand.prediction.utils;

import java.time.LocalDate;

import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponseForecast;
import ru.kabor.demand.prediction.entity.TimeMomentDescription;
import ru.kabor.demand.prediction.entity.TimeSeriesElement;
import ru.kabor.demand.prediction.entity.WhsArtTimeline;

/** It builds result of making forecast */
public class ResponseForecastBuilder {

	/** build success forecast response
	 * @param forecastParameters
	 * @param whsArtTimeline
	 * @param resultFromR
	 * @return
	 */
	public static ResponseForecast buildResponseForecast(RequestForecastParameterSingle forecastParameters, WhsArtTimeline whsArtTimeline, double[] resultFromR) {
		ResponseForecast result = new ResponseForecast();
		LocalDate trainingDateEnd = LocalDate.parse(forecastParameters.getTrainingEnd());
		result.setWhsId(forecastParameters.getWhsId());
		result.setArtId(forecastParameters.getArtId());
		result.getTimeMomentsActual().addAll(whsArtTimeline.getTimeMoments());
		
		for (int i = 0; i < resultFromR.length; i++) {
			trainingDateEnd = trainingDateEnd.plusDays(1);
			TimeMomentDescription timeMomentDescription = new TimeMomentDescription();
			timeMomentDescription.setTimeMoment(trainingDateEnd);
			timeMomentDescription.setSales(new TimeSeriesElement(resultFromR[i]));
			result.getTimeMomentsPrediction().add(timeMomentDescription);
		}
		return result;
	}
}
