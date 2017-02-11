package ru.kabor.demand.prediction.utils;

import java.time.LocalDate;

import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponceForecast;
import ru.kabor.demand.prediction.entity.TimeMomentDescription;
import ru.kabor.demand.prediction.entity.WhsArtTimeline;

public class ResponceForecastBuilder {
	public static ResponceForecast buildResponseForecast (RequestForecastParameterSingle forecastParameters, WhsArtTimeline whsArtTimeline, double[] resultFromR, WhsArtTimeline whsArtTimelineSlope){
		ResponceForecast result = new ResponceForecast();
		LocalDate trainingDateEnd = LocalDate.parse(forecastParameters.getTrainingEnd());
		result.setWhsId(forecastParameters.getWhsId());
		result.setArtId(forecastParameters.getArtId());
		result.getTimeMomentsActual().addAll(whsArtTimeline.getTimeMoments());
		result.getTimeMomentsSmoothed().addAll(whsArtTimelineSlope.getTimeMoments());
		for(int i=0; i<resultFromR.length; i++){
			trainingDateEnd = trainingDateEnd.plusDays(1);
			result.getTimeMomentsPrediction().add(new TimeMomentDescription(trainingDateEnd, resultFromR[i],-1.0));
		}
		return result;
	}
}
