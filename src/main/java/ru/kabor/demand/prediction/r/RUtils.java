package ru.kabor.demand.prediction.r;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.rosuda.REngine.REXP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponceForecast;
import ru.kabor.demand.prediction.entity.TimeMomentDescription;
import ru.kabor.demand.prediction.entity.WhsArtTimeline;
import ru.kabor.demand.prediction.utils.ResponceForecastBuilder;
import ru.kabor.demand.prediction.utils.SMOOTH_TYPE;

@Component
public class RUtils {

	@Value("${r.host}")
	private String rHost;
	@Value("${r.port}")
	private Integer rPort;
	@Value("${r.initialCountConnections}")
	private Integer rInitialCountConnections;
	@Value("${r.maxCountConnections}")
	private Integer rMaxCountConnections;
	@Value("${r.awaitIfBusyTimeoutMillisecond}")
	private Long rAwaitIfBusyTimeoutMillisecond;
	
	private RConnectionPoolImpl rConnectionPool;
	
	@PostConstruct
	public void initIt() throws Exception {
		this.rConnectionPool = new RConnectionPoolImpl();
		this.rConnectionPool.setLoginSettings(this.rHost, this.rPort);
		this.rConnectionPool.setPoolSettings(this.rInitialCountConnections, this.rMaxCountConnections, this.rAwaitIfBusyTimeoutMillisecond);
		List<String> connectionOpenCommanList = new ArrayList<>();
		connectionOpenCommanList.add("library('forecast')");
		//connectionOpenCommanList.add("library('GMDH')");
		this.rConnectionPool.setConnectionLifecycleCommands(connectionOpenCommanList, null, null);
		this.rConnectionPool.attachToRserve();
	}
	
	@PreDestroy
	public void cleanUp() throws Exception {
		this.rConnectionPool.detachFromRserve();
	}

	private static final Logger LOG = LoggerFactory.getLogger(RUtils.class);

	public ResponceForecast makePrediction(RequestForecastParameterSingle forecastParameters, WhsArtTimeline whsArtTimeline) throws Exception {
		if (whsArtTimeline.getTimeMoments().size() < 3) {
			return null;
		}
		WhsArtTimeline whsArtTimelineSlope = this.makeWhsArtTimelineSlope(whsArtTimeline,forecastParameters.getSmoothType());
		switch(forecastParameters.getForecastMethod()){
			case WINTER_HOLT:{
				return makePredictionHoltWinters(forecastParameters,whsArtTimeline,whsArtTimelineSlope);
			}
			
			case ARIMA_2_0_0:{
				return makePredictionArima200(forecastParameters,whsArtTimeline,whsArtTimelineSlope);
			}
			
			case NEURAL_NETWORK:{
				return makePredictionNeuralNetwork(forecastParameters,whsArtTimeline,whsArtTimelineSlope);
			}
		}
		return null;
	}
	
	@SuppressWarnings("unused")
	private List<WhsArtTimeline> makeWhsArtTimelineSlopeList(List<WhsArtTimeline> whsArtTimelineList, SMOOTH_TYPE slope_type) throws Exception {
		List<WhsArtTimeline> whsArtTimelineSlopeList = new ArrayList<WhsArtTimeline>();
		for(WhsArtTimeline whsArtTimeline : whsArtTimelineList){
			WhsArtTimeline whsArtTimelineSlope = this.makeWhsArtTimelineSlope(whsArtTimeline,slope_type);
			whsArtTimelineSlopeList.add(whsArtTimelineSlope);
		}
		return whsArtTimelineSlopeList;
	}

	private WhsArtTimeline makeWhsArtTimelineSlope(WhsArtTimeline whsArtTimeline, SMOOTH_TYPE slope_type) throws Exception {
		switch (slope_type) {
			case NO: {
				return makeWhsArtTimelineSmoothNo(whsArtTimeline);
			}
			case YES: {
				return makeWhsArtTimelineSmoothYes(whsArtTimeline);
			}
			default:{
				return makeWhsArtTimelineSmoothNo(whsArtTimeline);
			}
		}
	}

	private ResponceForecast makePredictionHoltWinters(RequestForecastParameterSingle forecastParameters,	WhsArtTimeline whsArtTimeline, WhsArtTimeline whsArtTimelineSlope) throws Exception {
		RCommonConnection connection = null;

		ResponceForecast result = new ResponceForecast();
		try {
			String salesValues = whsArtTimeline.getSalesSortedByDate();
			LocalDate startTraining = whsArtTimeline.getTimeMoments().get(0).getTimeMoment();
			Integer dayOfYear = startTraining.getDayOfYear();
			Integer year = startTraining.getYear();
			Integer countDaysInYear = 365;
			if(startTraining.isLeapYear()){
				countDaysInYear = 366;
			}

			connection = this.rConnectionPool.getConnection();
			connection.voidEval("myvector <- c(" + salesValues + ")");
			connection.voidEval("myts <-ts(myvector,  freq="+countDaysInYear+", start=c(" + year + "," + dayOfYear + "))");
			connection.voidEval("mytimeWinter <- HoltWinters(myts,beta=FALSE, gamma=FALSE)");
			connection.voidEval("mytimeforecast1 <- forecast.HoltWinters(mytimeWinter, h="+ forecastParameters.getForecastDuration() + ")");
			REXP f = connection.parseAndEval("myobj <- as.numeric(mytimeforecast1$mean)");
			double[] resultFromR = f.asDoubles();
			result = ResponceForecastBuilder.buildResponseForecast(forecastParameters,whsArtTimeline, resultFromR,whsArtTimelineSlope);
			return result;
			
		} catch (Exception e) {
			LOG.error("Forecast exception:" + e.toString());
			throw new Exception("Forecast exception:" + e.toString());
		} finally {
			if (connection != null) {
				try {
					this.rConnectionPool.releaseConnection(connection);
				} catch (Exception e) {
					LOG.error("Can't close r connection:" + e.toString());
				}
			}
		}
	}
	
	private ResponceForecast makePredictionArima200(RequestForecastParameterSingle forecastParameters, WhsArtTimeline whsArtTimeline, WhsArtTimeline whsArtTimelineSlope) throws Exception {
		RCommonConnection connection = null;
		ResponceForecast result = new ResponceForecast();
		try {

			String salesValues = whsArtTimeline.getSalesSortedByDate();
			LocalDate startTraining = whsArtTimeline.getTimeMoments().get(0).getTimeMoment();
			Integer dayOfYear = startTraining.getDayOfYear();
			Integer year = startTraining.getYear();
			Integer countDaysInYear = 365;
			if(startTraining.isLeapYear()){
				countDaysInYear = 366;
			}

			connection = this.rConnectionPool.getConnection();
			connection.voidEval("myvector <- c(" + salesValues + ")");
			connection.voidEval("myts <-ts(myvector,  freq=" + countDaysInYear + ", start=c(" + year + "," + dayOfYear + "))");
			connection.voidEval("mytsarima <- arima(myts, order=c(2,0,0))");
			connection.voidEval("mytimeforecast1 <-forecast.Arima(mytsarima, h="+ forecastParameters.getForecastDuration() + ")");
			
			REXP f = connection.parseAndEval("myobj <- as.numeric(mytimeforecast1$mean)");
			double[] resultFromR = f.asDoubles();

			result = ResponceForecastBuilder.buildResponseForecast(forecastParameters,whsArtTimeline, resultFromR,whsArtTimelineSlope);
			return result;
		} catch (Exception e) {
			LOG.error("Forecast exception:" + e.toString());
			throw new Exception("Forecast exception:" + e.toString());
		} finally {
			if (connection != null) {
				try {
					this.rConnectionPool.releaseConnection(connection);
				} catch (Exception e) {
					LOG.error("Can't close r connection:" + e.toString());
				}
			}
		}
	}
	
	private ResponceForecast makePredictionNeuralNetwork(RequestForecastParameterSingle forecastParameters, WhsArtTimeline whsArtTimeline, WhsArtTimeline whsArtTimelineSlope) throws Exception {
		RCommonConnection connection = null;
		ResponceForecast result = new ResponceForecast();
		try {

			String salesValues = whsArtTimeline.getSalesSortedByDate();
			LocalDate startTraining = whsArtTimeline.getTimeMoments().get(0).getTimeMoment();
			Integer dayOfYear = startTraining.getDayOfYear();
			Integer year = startTraining.getYear();
			Integer countDaysInYear = 365;
			if(startTraining.isLeapYear()){
				countDaysInYear = 366;
			}
			Integer forecastDuration = forecastParameters.getForecastDuration();	//That method allows not more that 5 days prediction
			if (forecastDuration > 5) {
				forecastDuration = 5;
			}

			connection = this.rConnectionPool.getConnection();
			connection.voidEval("myvector <- c(" + salesValues + ")");
			connection.voidEval("myts <-ts(myvector,  freq=" + countDaysInYear + ", start=c(" + year + "," + dayOfYear + "))");
			//connection.voidEval("mytimeforecast1 <- fcast(myts, method = 'GMDH', input = 3, layer = 2, f.number = "	+ forecastDuration + ", level = 95, tf = 'sigmoid')");
			REXP f = connection.parseAndEval("myobj <- as.numeric(mytimeforecast1$mean)");
			double[] resultFromR = f.asDoubles();

			result = ResponceForecastBuilder.buildResponseForecast(forecastParameters,whsArtTimeline, resultFromR,whsArtTimelineSlope);
			return result;
		} catch (Exception e) {
			LOG.error("Forecast exception:" + e.toString());
			throw new Exception("Forecast exception:" + e.toString());
		} finally {
			if (connection != null) {
				try {
					this.rConnectionPool.releaseConnection(connection);
				} catch (Exception e) {
					LOG.error("Can't close r connection:" + e.toString());
				}
			}
		}
	}
	
	private WhsArtTimeline makeWhsArtTimelineSmoothYes(WhsArtTimeline whsArtTimeline) throws Exception {
		// Count simple statistic
		WhsArtTimeline slopeTimeline = new WhsArtTimeline(whsArtTimeline.getWhsId(), whsArtTimeline.getArtId(), whsArtTimeline.getTimeMoments());
		Integer countDaysWithoutSales = 0;
		Double averageSale = 0.0;
		
		for (TimeMomentDescription timeMoment : slopeTimeline.getTimeMoments()) {
			if (timeMoment.getSalesQnty() <= 0) {
				countDaysWithoutSales++;
			} else {
				averageSale += timeMoment.getSalesQnty();
			}
		}
		averageSale = averageSale / (slopeTimeline.getTimeMoments().size() - countDaysWithoutSales);
		
		// Work with days without sales
		for (TimeMomentDescription timeMoment : slopeTimeline.getTimeMoments()) {
			if (timeMoment.getSalesQnty() <= 0) {
				timeMoment.setSalesQnty(averageSale);
			}
		}
		// Count slope in R
		List<Double> loessProcessList = this.makeLoess(slopeTimeline);
		// Detect and eliminate extremums
		for (int i = 0; i < slopeTimeline.getTimeMoments().size(); i++) {
			Double deviation = (slopeTimeline.getTimeMoments().get(i).getSalesQnty() - averageSale)/averageSale;
			if(Math.abs(deviation)>0.4){
				slopeTimeline.getTimeMoments().get(i).setSalesQnty(loessProcessList.get(i));
			}
		}
		return slopeTimeline;
	}
	
	private WhsArtTimeline makeWhsArtTimelineSmoothNo(WhsArtTimeline whsArtTimeline) {
		WhsArtTimeline slopeTimeline = new WhsArtTimeline(whsArtTimeline.getWhsId(), whsArtTimeline.getArtId(), whsArtTimeline.getTimeMoments());
		return slopeTimeline;
	}
	
	private List<Double> makeLoess(WhsArtTimeline artTimeline) throws Exception{
		RCommonConnection connection = null;
		List<Double> result = new ArrayList<Double>(artTimeline.getTimeMoments().size());
		try {
			connection = this.rConnectionPool.getConnection();
			String salesValues = artTimeline.getSalesSortedByDate();
			LocalDate startTraining = artTimeline.getTimeMoments().get(0).getTimeMoment();
			Integer dayOfYear = startTraining.getDayOfYear();
			Integer year = startTraining.getYear();
			Integer countDaysInYear = 365;
			
			if(startTraining.isLeapYear()){
				countDaysInYear = 366;
			}
			
			connection.voidEval("myvector <- c(" + salesValues + ")");
			connection.voidEval("myts <-ts(myvector,  freq=" + countDaysInYear + ", start=c(" + year + "," + dayOfYear + "))");
			connection.voidEval("timeMoments = 1:length(myts)");
			connection.voidEval("timeSeries = myts");
			connection.voidEval("mySmoothing <- loess(timeSeries~timeMoments)");
			connection.voidEval("forecast = predict(mySmoothing)");
			REXP f = connection.parseAndEval("myobj <- as.numeric(forecast)");
			double[] resultFromR = f.asDoubles();
			for(int i=0;i<resultFromR.length;i++){
				result.add(resultFromR[i]);
			}
		} catch (Exception e) {
			LOG.error("Forecast exception:" + e.toString());
			throw new Exception("Smooth Loess exception:" + e.toString());
		} finally {
			if (connection != null) {
				try {
					this.rConnectionPool.releaseConnection(connection);
				} catch (Exception e) {
					LOG.error("Can't close r connection:" + e.toString());
				}
			}
		}
		return result;
	}
}
