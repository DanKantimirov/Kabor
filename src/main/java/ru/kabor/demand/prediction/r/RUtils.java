package ru.kabor.demand.prediction.r;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.rosuda.REngine.REXP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ru.kabor.demand.prediction.entity.RequestElasticityParameterSingle;
import ru.kabor.demand.prediction.entity.RequestForecastAndElasticityParameterSingle;
import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponseElasticity;
import ru.kabor.demand.prediction.entity.ResponseForecast;
import ru.kabor.demand.prediction.entity.ResponseForecastAndElasticity;
import ru.kabor.demand.prediction.entity.SalesAndPriceDeviation;
import ru.kabor.demand.prediction.entity.TimeMomentDescription;
import ru.kabor.demand.prediction.entity.WhsArtTimeline;
import ru.kabor.demand.prediction.service.DataServiceException;
import ru.kabor.demand.prediction.utils.ResponseElasticityBuilder;
import ru.kabor.demand.prediction.utils.ResponseForecastBuilder;
import ru.kabor.demand.prediction.utils.SMOOTH_TYPE;

/** It contains all commands for interaction with R */
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
	
	private static final Logger LOG = LoggerFactory.getLogger(RUtils.class);
	
	/** @PostConstruct
	 * @throws Exception
	 */
	@PostConstruct
	public void initIt() throws Exception {
		this.rConnectionPool = new RConnectionPoolImpl();
		this.rConnectionPool.setLoginSettings(this.rHost, this.rPort);
		this.rConnectionPool.setPoolSettings(this.rInitialCountConnections, this.rMaxCountConnections, this.rAwaitIfBusyTimeoutMillisecond);
		List<String> connectionOpenCommanList = new ArrayList<>();
		connectionOpenCommanList.add("library('forecast')");
		connectionOpenCommanList.add("library('dplyr')");
		connectionOpenCommanList.add("library('GMDH')");
		connectionOpenCommanList.add("library('stlplus')");
		this.rConnectionPool.setConnectionLifecycleCommands(connectionOpenCommanList, null, null);
		this.rConnectionPool.attachToRserve();
	}
	
	/** @PreDestroy
	 * @throws Exception
	 */
	@PreDestroy
	public void cleanUp() throws Exception {
		this.rConnectionPool.detachFromRserve();
	}
	
	
	/** Calculate forecast in R
	 * @param forecastParameters parameters of forecast
	 * @param whsArtTimeline sales and rests
	 * @return	response with result of forecast
	 * @throws Exception
	 */
	public ResponseForecast makeForecast(RequestForecastParameterSingle forecastParameters, WhsArtTimeline whsArtTimeline) throws Exception {
		
		LOG.info("Forecast. whs_id:" + forecastParameters.getWhsId() + " art_id:" + forecastParameters.getArtId());
		
		if (whsArtTimeline.getTimeMoments().size() < 3) {
			return null;
		}
		
		switch(forecastParameters.getForecastMethod()){
		
    		case AUTO_CHOOSE:{
    			return makePredictionAutoChoose(forecastParameters,whsArtTimeline);
    		}
			case HOLT_WINTERS:{
				return makePredictionHoltWinters(forecastParameters,whsArtTimeline);
			}
			case ARIMA_AUTO:{
				return makePredictionArimaAuto(forecastParameters,whsArtTimeline);
			}
			case NEURAL_NETWORK:{
				return makePredictionNeuralNetwork(forecastParameters,whsArtTimeline);
			}
			case ETS:{
				return makePredictionETS(forecastParameters,whsArtTimeline);
			}
			case TBATS:{
				return makePredictionTBATS(forecastParameters,whsArtTimeline);
			}
		}
		return null;
	}
	
	/** Use ARIMA, ETS and TBATS for making forecast (choose the best mode) 
	 * @param forecastParameters parameters of forecast
	 * @param whsArtTimeline sales and rests
	 * @return response with result of forecast
	 * @throws Exception
	 */
	private ResponseForecast makePredictionAutoChoose(RequestForecastParameterSingle forecastParameters, WhsArtTimeline whsArtTimeline) throws Exception {
		//Select between ARIMA, ETS and TBATS
		RCommonConnection connection = null;
		ResponseForecast result = new ResponseForecast();
		try {

			String salesSmoothedValues = whsArtTimeline.getSalesSmootedSortedByDate();
			LocalDate startTraining = whsArtTimeline.getTimeMoments().get(0).getTimeMoment();
			Integer dayOfYear = startTraining.getDayOfYear();
			Integer year = startTraining.getYear();

			connection = this.rConnectionPool.getConnection();
			connection.voidEval("myvector <- c(" + salesSmoothedValues + ")");
			connection.voidEval("myts <-ts(myvector,  freq=7, start=c(" + year + "," + dayOfYear + "))");
			
			connection.voidEval("mytsets <- ets(y=myts, additive.only=FALSE)");
			connection.voidEval("mytsarima <- auto.arima(y=myts, seasonal=TRUE)");
			connection.voidEval("mytstbats <- tbats(myts)");

			
			REXP etsAICRexp = connection.parseAndEval("etsAIC <- mytsets$aic");
			REXP arimaAICRexp = connection.parseAndEval("arimaAIC <- mytsarima$aic");
			REXP tbatsAICRexp = connection.parseAndEval("tbatsAIC <- mytstbats$AIC");
			
			Double etsAIC = Double.MAX_VALUE;
			Double arimaAIC = Double.MAX_VALUE;
			Double tbatsAIC = Double.MAX_VALUE;

			if (!etsAICRexp.isNull()) {
				etsAIC = etsAICRexp.asDouble();
			}
			if (!arimaAICRexp.isNull()) {
				arimaAIC = arimaAICRexp.asDouble();
			}
			if (!tbatsAICRexp.isNull()) {
				tbatsAIC = tbatsAICRexp.asDouble();
			}
			
			if(etsAIC <= arimaAIC && etsAIC <= tbatsAIC){
				connection.voidEval("mytimeforecast1 <-forecast(mytsets, h="+ forecastParameters.getForecastDuration() + ")");
			} else if(arimaAIC <= etsAIC && arimaAIC <= tbatsAIC){
				connection.voidEval("mytimeforecast1 <-forecast(mytsarima, h="+ forecastParameters.getForecastDuration() + ")");
			} else{
				connection.voidEval("mytimeforecast1 <-forecast(mytstbats, h="+ forecastParameters.getForecastDuration() + ")");
			}
			
			REXP forecastREXP = connection.parseAndEval("myobj <- as.numeric(mytimeforecast1$mean)");
			
			double[] resultFromR = forecastREXP.asDoubles();
			result = ResponseForecastBuilder.buildResponseForecast(forecastParameters,whsArtTimeline, resultFromR);
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
	
	/** Use TBATS for making forecast 
	 * @param forecastParameters parameters of forecast
	 * @param whsArtTimeline sales and rests
	 * @return response with result of forecast
	 * @throws Exception
	 */
	private ResponseForecast makePredictionTBATS(RequestForecastParameterSingle forecastParameters, WhsArtTimeline whsArtTimeline) throws Exception {
		RCommonConnection connection = null;
		ResponseForecast result = new ResponseForecast();
		try {

			String salesSmootedValues = whsArtTimeline.getSalesSmootedSortedByDate();
			LocalDate startTraining = whsArtTimeline.getTimeMoments().get(0).getTimeMoment();
			Integer dayOfYear = startTraining.getDayOfYear();
			Integer year = startTraining.getYear();

			connection = this.rConnectionPool.getConnection();
			connection.voidEval("myvector <- c(" + salesSmootedValues + ")");
			connection.voidEval("myts <-ts(myvector,  freq=7, start=c(" + year + "," + dayOfYear + "))");
			connection.voidEval("mytstbats <- tbats(y=myts, seasonal.periods=7)");
			connection.voidEval("mytimeforecast1 <-forecast(mytstbats, h="+ forecastParameters.getForecastDuration() + ")");
			
			REXP forecastREXP = connection.parseAndEval("myobj <- as.numeric(mytimeforecast1$mean)");
			double[] resultFromR = forecastREXP.asDoubles();

			result = ResponseForecastBuilder.buildResponseForecast(forecastParameters,whsArtTimeline, resultFromR);
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
	
	/** Use ETS for making forecast 
	 * @param forecastParameters parameters of forecast
	 * @param whsArtTimeline sales and rests
	 * @return response with result of forecast
	 * @throws Exception
	 */
	private ResponseForecast makePredictionETS(RequestForecastParameterSingle forecastParameters, WhsArtTimeline whsArtTimeline) throws Exception {
		RCommonConnection connection = null;
		ResponseForecast result = new ResponseForecast();
		try {

			String salesSmootedValues = whsArtTimeline.getSalesSmootedSortedByDate();
			LocalDate startTraining = whsArtTimeline.getTimeMoments().get(0).getTimeMoment();
			Integer dayOfYear = startTraining.getDayOfYear();
			Integer year = startTraining.getYear();

			connection = this.rConnectionPool.getConnection();
			connection.voidEval("myvector <- c(" + salesSmootedValues + ")");
			connection.voidEval("myts <-ts(myvector,  freq=7, start=c(" + year + "," + dayOfYear + "))");
			connection.voidEval("mytsets <- ets(y=myts, additive.only=FALSE)");
			connection.voidEval("mytimeforecast1 <-forecast(mytsets, h="+ forecastParameters.getForecastDuration() + ")");
			
			REXP forecastREXP = connection.parseAndEval("myobj <- as.numeric(mytimeforecast1$mean)");
			double[] resultFromR = forecastREXP.asDoubles();

			result = ResponseForecastBuilder.buildResponseForecast(forecastParameters,whsArtTimeline, resultFromR);
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

	
	/** Calculate sales smoothed values for whsArtTimeline
	 * @param whsArtTimeline sales and rests
	 * @param slope_type type of slope
	 * @throws Exception
	 */
	public void calculateWhsArtTimelineSlope(WhsArtTimeline whsArtTimeline, SMOOTH_TYPE slope_type) throws Exception {
		switch (slope_type) {
			case NO: {
				makeWhsArtTimelineSmoothNo(whsArtTimeline);
				return;
			}
			case YES: {
				makeWhsArtTimelineSmoothYes(whsArtTimeline);
				return;
			}
			default:{
				makeWhsArtTimelineSmoothNo(whsArtTimeline);
				return;
			}
		}
	}
	
	/** Calculate sales trend, seasonal and random values for whsArtTimeline
	 * @param whsArtTimeline sales and rests
	 * @throws Exception
	 */
	public void calculateWhsArtTimelineTrendSeasonalAndRandom(WhsArtTimeline whsArtTimeline) throws Exception {
		// Make timeSeries
		RCommonConnection connection = null;
		try {
			String salesSmootedValues = whsArtTimeline.getSalesSmootedSortedByDate();
			LocalDate startTraining = whsArtTimeline.getTimeMoments().get(0).getTimeMoment();
			Integer dayOfYear = startTraining.getDayOfYear();
			Integer year = startTraining.getYear();

			connection = this.rConnectionPool.getConnection();
			connection.voidEval("myvector <- c(" + salesSmootedValues + ")");
			connection.voidEval("myts <-ts(myvector,  freq=7, start=c(" + year + "," + dayOfYear + "))");
			connection.voidEval("mytsdecompose<-stlplus(myts,t.window=7, s.window='periodic', robust=TRUE)");

			REXP seasonalREXP = connection.parseAndEval("mytsdecomposeseasonal <- as.numeric(mytsdecompose$data$seasonal)");
			REXP trendREXP = connection.parseAndEval("mytsdecomposetrend <- as.numeric(mytsdecompose$data$trend)");
			REXP remainderREXP = connection.parseAndEval("mytsdecomposeseasonal <- as.numeric(mytsdecompose$data$remainder)");

			double[] resultSeasonal = seasonalREXP.asDoubles();
			double[] resultTrend = trendREXP.asDoubles();
			double[] resultRemainder = remainderREXP.asDoubles();

			for (int i = 0; i < whsArtTimeline.getTimeMoments().size(); i++) {
				TimeMomentDescription timeMomentDescription = whsArtTimeline.getTimeMoments().get(i);
				timeMomentDescription.getSales().setTrendValue(resultTrend[i]);
				timeMomentDescription.getSales().setSeasonalValue(resultSeasonal[i]);
				timeMomentDescription.getSales().setRandomValue(resultRemainder[i]);
			}

		} catch (Exception e) {
			LOG.error("Forecast exception:" + e.toString());
			throw new Exception("Trend detection exception:" + e.toString());
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
	
	/** Use HoltWinters for making forecast 
	 * @param forecastParameters parameters of forecast
	 * @param whsArtTimeline sales and rests
	 * @return response with result of forecast
	 * @throws Exception
	 */
	private ResponseForecast makePredictionHoltWinters(RequestForecastParameterSingle forecastParameters,	WhsArtTimeline whsArtTimeline) throws Exception {
		RCommonConnection connection = null;

		ResponseForecast result = new ResponseForecast();
		try {
			String salesSmootedValues = whsArtTimeline.getSalesSmootedSortedByDate();
			LocalDate startTraining = whsArtTimeline.getTimeMoments().get(0).getTimeMoment();
			Integer dayOfYear = startTraining.getDayOfYear();
			Integer year = startTraining.getYear();

			connection = this.rConnectionPool.getConnection();
			connection.voidEval("myvector <- c(" + salesSmootedValues + ")");
			connection.voidEval("myts <-ts(myvector,  freq=7, start=c(" + year + "," + dayOfYear + "))");
			connection.voidEval("mytimeWinter <- HoltWinters(myts,beta=FALSE, gamma=FALSE)");
			connection.voidEval("mytimeforecast1 <- forecast.HoltWinters(mytimeWinter, h="+ forecastParameters.getForecastDuration() + ")");
			REXP forecastREXP = connection.parseAndEval("myobj <- as.numeric(mytimeforecast1$mean)");
			double[] resultFromR = forecastREXP.asDoubles();
			result = ResponseForecastBuilder.buildResponseForecast(forecastParameters,whsArtTimeline, resultFromR);
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
	
	/** Use ARIMA for making forecast 
	 * @param forecastParameters parameters of forecast
	 * @param whsArtTimeline sales and rests
	 * @return response with result of forecast
	 * @throws Exception
	 */
	private ResponseForecast makePredictionArimaAuto(RequestForecastParameterSingle forecastParameters, WhsArtTimeline whsArtTimeline) throws Exception {
		RCommonConnection connection = null;
		ResponseForecast result = new ResponseForecast();
		try {
			
			String salesSmootedValues = whsArtTimeline.getSalesSmootedSortedByDate();
			LocalDate startTraining = whsArtTimeline.getTimeMoments().get(0).getTimeMoment();
			Integer dayOfYear = startTraining.getDayOfYear();
			Integer year = startTraining.getYear();

			connection = this.rConnectionPool.getConnection();
			connection.voidEval("myvector <- c(" + salesSmootedValues + ")");
			connection.voidEval("myts <-ts(myvector,  freq=7, start=c(" + year + "," + dayOfYear + "))");
			connection.voidEval("mytsarima <- auto.arima(y=myts, seasonal=TRUE)");
			connection.voidEval("mytimeforecast1 <-forecast(mytsarima, h="+ forecastParameters.getForecastDuration() + ")");
			
			REXP forecastREXP = connection.parseAndEval("myobj <- as.numeric(mytimeforecast1$mean)");
			double[] resultFromR = forecastREXP.asDoubles();

			result = ResponseForecastBuilder.buildResponseForecast(forecastParameters,whsArtTimeline, resultFromR);
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
	
	/** Use NeuralNetwork for making forecast (only 5 days)
	 * @param forecastParameters parameters of forecast
	 * @param whsArtTimeline sales and rests
	 * @return response with result of forecast
	 * @throws Exception
	 */
	private ResponseForecast makePredictionNeuralNetwork(RequestForecastParameterSingle forecastParameters, WhsArtTimeline whsArtTimeline) throws Exception {
		RCommonConnection connection = null;
		ResponseForecast result = new ResponseForecast();
		try {

			String salesSmootedValues = whsArtTimeline.getSalesSmootedSortedByDate();
			LocalDate startTraining = whsArtTimeline.getTimeMoments().get(0).getTimeMoment();
			Integer dayOfYear = startTraining.getDayOfYear();
			Integer year = startTraining.getYear();
			
			Integer forecastDuration = forecastParameters.getForecastDuration();	//That method allows not more that 5 days prediction
			if (forecastDuration > 5) {
				forecastDuration = 5;
			}

			connection = this.rConnectionPool.getConnection();
			connection.voidEval("myvector <- c(" + salesSmootedValues + ")");
			connection.voidEval("myts <-ts(myvector,  freq=7, start=c(" + year + "," + dayOfYear + "))");
			connection.voidEval("mytimeforecast1 <- fcast(myts, method = 'GMDH', input = 3, layer = 2, f.number = "	+ forecastDuration + ", level = 95, tf = 'sigmoid')");
			REXP forecastREXP = connection.parseAndEval("myobj <- as.numeric(mytimeforecast1$mean)");
			double[] resultFromR = forecastREXP.asDoubles();

			result = ResponseForecastBuilder.buildResponseForecast(forecastParameters,whsArtTimeline, resultFromR);
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
	
	/** Calculate sales smoothed values for whsArtTimeline when slope_type = Yes
	 * @param whsArtTimeline sales and rests
	 * @throws Exception
	 */
	private void makeWhsArtTimelineSmoothYes(WhsArtTimeline whsArtTimeline) throws Exception {
		Integer countDaysWithoutSales = 0;
		Double averageSale = 0.0;
		for (TimeMomentDescription timeMoment : whsArtTimeline.getTimeMoments()) {
			if (timeMoment.getSales().getActualValue() <= 0) {
				countDaysWithoutSales++;
			} else {
				averageSale += timeMoment.getSales().getActualValue();
			}
		}
		
		averageSale = averageSale / (whsArtTimeline.getTimeMoments().size() - countDaysWithoutSales);
		// Count slope in R
		List<Double> loessProcessList = this.makeLoess(whsArtTimeline);
		// Detect and eliminate extremums
		for (int i = 0; i < whsArtTimeline.getTimeMoments().size(); i++) {
			Double deviation = (whsArtTimeline.getTimeMoments().get(i).getSales().getActualValue() - averageSale)/averageSale;
			if(Math.abs(deviation)>0.4){
				whsArtTimeline.getTimeMoments().get(i).getSales().setSmoothedValue(loessProcessList.get(i));	//set smoothed value
			} else{
				whsArtTimeline.getTimeMoments().get(i).getSales().setSmoothedValue(whsArtTimeline.getTimeMoments().get(i).getSales().getActualValue());		//set actual value
			}
		}
		return;
	}
	
	/** Calculate sales smoothed values for whsArtTimeline when slope_type = No
	 * @param whsArtTimeline sales and rests
	 * @throws Exception
	 */
	private void makeWhsArtTimelineSmoothNo(WhsArtTimeline whsArtTimeline) {
		for(TimeMomentDescription timeMoment:whsArtTimeline.getTimeMoments()){
			timeMoment.getSales().setSmoothedValue(timeMoment.getSales().getActualValue());
		}
	}
	
	/** Make smoothing by sales and rests
	 * @param artTimeline sales and rests
	 * @return List of smoothed values (Double)
	 * @throws Exception
	 */
	private List<Double> makeLoess(WhsArtTimeline artTimeline) throws Exception{
		RCommonConnection connection = null;
		List<Double> result = new ArrayList<Double>(artTimeline.getTimeMoments().size());
		try {
			connection = this.rConnectionPool.getConnection();
			String salesValues = artTimeline.getSalesActualSortedByDate();
			LocalDate startTraining = artTimeline.getTimeMoments().get(0).getTimeMoment();
			Integer dayOfYear = startTraining.getDayOfYear();
			Integer year = startTraining.getYear();
			
			connection.voidEval("myvector <- c(" + salesValues + ")");
			connection.voidEval("myts <-ts(myvector,  freq=7, start=c(" + year + "," + dayOfYear + "))");
			connection.voidEval("timeMoments = 1:length(myts)");
			connection.voidEval("timeSeries = myts");
			connection.voidEval("mySmoothing <- loess(timeSeries~timeMoments)");
			connection.voidEval("forecast = predict(mySmoothing)");
			REXP forecastREXP = connection.parseAndEval("myobj <- as.numeric(forecast)");
			double[] resultFromR = forecastREXP.asDoubles();
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
	
	/** Prepare Arrasy of price and sales differences
	 * @param whsArtTimeline sales, rests and prices
	 * @return arrasys of price and sales differences
	 */
	private List<SalesAndPriceDeviation> prepareXdataAndYdataForElasticity(WhsArtTimeline whsArtTimeline) {
		List<SalesAndPriceDeviation> salesAndPriceDeviationList = new ArrayList<>();

		if (whsArtTimeline == null || whsArtTimeline.getTimeMoments() == null || whsArtTimeline.getTimeMoments().size() < 2) {
			return salesAndPriceDeviationList;
		}

		// sort timeline by date
		List<TimeMomentDescription> timeMomentsList = whsArtTimeline.getTimeMoments();
		timeMomentsList = timeMomentsList.stream().filter(e -> e.getPriceQnty() != null && e.getPriceQnty() > 0).collect(Collectors.toList());

		if (whsArtTimeline.getTimeMoments().size() < 2) {
			return salesAndPriceDeviationList;
		}

		Collections.sort(timeMomentsList, new Comparator<TimeMomentDescription>() {
			@Override
			public int compare(TimeMomentDescription arg0, TimeMomentDescription arg1) {
				return arg0.getTimeMoment().compareTo(arg1.getTimeMoment());
			}
		});

		for (int i = 0; i < timeMomentsList.size() - 1; i++) {
			Double priceOld = timeMomentsList.get(i).getPriceQnty();
			Double salesOld = timeMomentsList.get(i).getSales().getTrendValue();
			Double priceNew = timeMomentsList.get(i + 1).getPriceQnty();
			Double salesNew = timeMomentsList.get(i + 1).getSales().getTrendValue();
			if(!priceOld.equals(priceNew)){
    			SalesAndPriceDeviation deviation = new SalesAndPriceDeviation(priceNew - priceOld, salesNew - salesOld);
    			if (!salesAndPriceDeviationList.contains(deviation)) {
    				salesAndPriceDeviationList.add(deviation);
    			}
			}
		}
		return salesAndPriceDeviationList;
	}
	
	/** Get sales difference connected by comma
	 * @param salesAndPricesDeviationLists arrasys of price and sales differences
	 * @return sales difference connected by comma
	 */
	private String getSalesDeviationFormatted(List<SalesAndPriceDeviation> salesAndPricesDeviationLists){
		String result = salesAndPricesDeviationLists.stream().map (e -> e.getSalesDeviation().toString()).collect(Collectors.joining (","));
		return result;
	}
	
	/** Get prices difference connected by comma
	 * @param salesAndPricesDeviationLists
	 * @return prices difference connected by comma
	 */
	private String getPricesDeviationFormatted(List<SalesAndPriceDeviation> salesAndPricesDeviationLists){
		String result = salesAndPricesDeviationLists.stream().map (e -> e.getPriceDeviatin().toString()).collect(Collectors.joining (","));
		return result;
	}

	/** Calculate elasticity
	 * @param elasticityParameter parameters of calculating elasticity
	 * @param whsArtTimeline sales and rests
	 * @param isResultWithTimeMoments include input time moments in result 
	 * @return response of elasticity
	 * @throws Exception
	 */
	public ResponseElasticity makeElasticity(RequestElasticityParameterSingle elasticityParameter, WhsArtTimeline whsArtTimeline, Boolean isResultWithTimeMoments) throws Exception  {
		
		LOG.info("Elasticity. whs_id:" + elasticityParameter.getWhsId() + " art_id:" + elasticityParameter.getArtId());
		if (whsArtTimeline.getTimeMoments().size() < 3) {
			return null;
		}
		
		RCommonConnection connection = null;
		
		try {
			
			List<SalesAndPriceDeviation> salesAndPricesDeviationLists = prepareXdataAndYdataForElasticity(whsArtTimeline);
			if(salesAndPricesDeviationLists == null || salesAndPricesDeviationLists.size()<2){
				throw new DataServiceException("Can't calculate elasticity, too little dates");
			}
			
			// Calculate models
			String priceValuse = getPricesDeviationFormatted(salesAndPricesDeviationLists);
			String salesValues = getSalesDeviationFormatted(salesAndPricesDeviationLists);
			
			connection = this.rConnectionPool.getConnection();
			String xDataExp = "xdata <- c(" + priceValuse + ")";
			String yDataExp = "ydata <- c(" + salesValues + ")";
			
			connection.voidEval(xDataExp);
			connection.voidEval(yDataExp);
			
			List<REXP> resultModelsErrorList = new ArrayList<>();
			List<REXP> resultModelsFormulaList = new ArrayList<>();
			List<REXP> resultModelsParameterList = new ArrayList<>();
			
			if(salesAndPricesDeviationLists.size()>2){
    			try {
    				connection.voidEval("model1<-nls(ydata ~ p1*cos(p2*xdata) + p2*sin(p1*xdata), start=list(p1=1,p2=0.2), control = list(maxiter = 100))");
    				connection.voidEval("resultModel1 <- summary(model1)");
    				REXP modelSummary = connection.parseAndEval("resultModel1$sigma");
    				resultModelsErrorList.add(modelSummary);
    				REXP modelFormula = connection.parseAndEval("toString(resultModel1$formula)");
    				resultModelsFormulaList.add(modelFormula);
    				REXP modelParameter = connection.parseAndEval("resultModel1$coefficients[, 'Estimate']");
    				resultModelsParameterList.add(modelParameter);
    			} catch (Exception e) {	}
    			try {
    				connection.voidEval("model2<-nls(ydata ~ p1*cos(p2*xdata) + p3, start=list(p1=1,p2=0.2,p3=0.2), control = list(maxiter = 100))");
    				connection.voidEval("resultModel2 <- summary(model2)");
    				REXP modelSummary = connection.parseAndEval("resultModel2$sigma");
    				resultModelsErrorList.add(modelSummary);
    				REXP modelFormula = connection.parseAndEval("toString(resultModel2$formula)");
    				resultModelsFormulaList.add(modelFormula);
    				REXP modelParameter = connection.parseAndEval("resultModel2$coefficients[, 'Estimate']");
    				resultModelsParameterList.add(modelParameter);
    			} catch (Exception e) {	}
    			try {
    				connection.voidEval("model3<-nls(ydata ~ p1*xdata*xdata + p2, start=list(p1=1,p2=0.2), control = list(maxiter = 100))");
    				connection.voidEval("resultModel3 <- summary(model3)");
    				REXP modelSummary = connection.parseAndEval("resultModel3$sigma");
    				resultModelsErrorList.add(modelSummary);
    				REXP modelFormula = connection.parseAndEval("toString(resultModel3$formula)");
    				resultModelsFormulaList.add(modelFormula);
    				REXP modelParameter = connection.parseAndEval("resultModel3$coefficients[, 'Estimate']");
    				resultModelsParameterList.add(modelParameter);
    			} catch (Exception e) {	}
    			try {
    				connection.voidEval("model4<-nls(ydata ~ p1*xdata + p2, start=list(p1=1,p2=0.2), control = list(maxiter = 100))");
    				connection.voidEval("resultModel4 <- summary(model4)");
    				REXP modelSummary = connection.parseAndEval("resultModel4$sigma");
    				resultModelsErrorList.add(modelSummary);
    				REXP modelFormula = connection.parseAndEval("toString(resultModel4$formula)");
    				resultModelsFormulaList.add(modelFormula);
    				REXP modelParameter = connection.parseAndEval("resultModel4$coefficients[, 'Estimate']");
    				resultModelsParameterList.add(modelParameter);
    			} catch (Exception e) {	}
    			try {
    				connection.voidEval("model5<-nls(ydata ~ p1^xdata + p2, start=list(p1=1,p2=0.2), control = list(maxiter = 100))");
    				connection.voidEval("resultModel5 <- summary(model5)");
    				REXP modelSummary = connection.parseAndEval("resultModel5$sigma");
    				resultModelsErrorList.add(modelSummary);
    				REXP modelFormula = connection.parseAndEval("toString(resultModel5$formula)");
    				resultModelsFormulaList.add(modelFormula);
    				REXP modelParameter = connection.parseAndEval("resultModel5$coefficients[, 'Estimate']");
    				resultModelsParameterList.add(modelParameter);
    			} catch (Exception e) {	}
    			try {
    				connection.voidEval("model6<-nls(ydata ~ p1*log(xdata, base=p2) + p3, start=list(p1=1,p2=0.2,p3=0.2), control = list(maxiter = 100))");
    				connection.voidEval("resultModel6 <- summary(model6)");
    				REXP modelSummary = connection.parseAndEval("resultModel6$sigma");
    				resultModelsErrorList.add(modelSummary);
    				REXP modelFormula = connection.parseAndEval("toString(resultModel6$formula)");
    				resultModelsFormulaList.add(modelFormula);
    				REXP modelParameter = connection.parseAndEval("resultModel6$coefficients[, 'Estimate']");
    				resultModelsParameterList.add(modelParameter);
    			} catch (Exception e) {	} 
			}
			
			double [] lineCoef = null;
			try{
				connection.voidEval("goods <-  data.frame(" + xDataExp + "," + yDataExp  +")");
				connection.voidEval("modelLine <- lm(formula = ydata ~ xdata, data = goods)");
				connection.voidEval("resultModelLine <- summary(modelLine)");
				REXP modelParameter = connection.parseAndEval("resultModelLine$coefficients[,1]");
				lineCoef = modelParameter.asDoubles();
			} catch (Exception e) {	} 
			
			//Find Best
			Double bestModelError = null;
			String bestModelFormula = null;
			double[] bestModelCoeff= {};
			Double lessError = 99999.9;
			Boolean hadOnePrediction = false;
			
			for(int i=0; i<resultModelsErrorList.size();i++){
				hadOnePrediction = true;
				if (lessError > resultModelsErrorList.get(i).asDouble()) {
					bestModelError = resultModelsErrorList.get(i).asDouble();
					bestModelFormula = resultModelsFormulaList.get(i).asString();
					bestModelCoeff = resultModelsParameterList.get(i).asDoubles();
					lessError = bestModelError;
				}
			}
			
			if(hadOnePrediction == false){
				//if nothig good, just line formula
				bestModelFormula = "ydata,~,p2*xdata + p1";
				bestModelCoeff = lineCoef;
				bestModelError = 0.0;
			}
			ResponseElasticity result = new ResponseElasticity();
			
			result = ResponseElasticityBuilder.buildSuccessResponseElasticity(elasticityParameter, whsArtTimeline, bestModelFormula, bestModelCoeff, bestModelError,isResultWithTimeMoments);
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

	/** Calculate forecast and elasticity together
	 * @param requestParameter	parameters of forecast and elasticity
	 * @param whsArtTimeline sales and rests
	 * @return result of forecast and elasticity together
	 */
	public ResponseForecastAndElasticity makeForecastAndElasticity(RequestForecastAndElasticityParameterSingle requestParameter, WhsArtTimeline whsArtTimeline) {
		ResponseForecast responseForecast = null;
		ResponseElasticity responseElasticity = null;
		LOG.info("Forecast and Elasticity. whs_id:" + requestParameter.getRequestForecastParameter().getWhsId() + " art_id:" + requestParameter.getRequestForecastParameter().getArtId());
		try {
			responseForecast = this.makeForecast(requestParameter.getRequestForecastParameter(), whsArtTimeline);
		} catch (Exception e) {
			responseForecast = new ResponseForecast(requestParameter.getRequestForecastParameter().getWhsId(), requestParameter.getRequestForecastParameter().getArtId());
			responseForecast.setErrorMessage("Can't make forecast:" + e.toString());
			LOG.error("Can't make forecast:" + e.toString());
		}

		try {
			responseElasticity = this.makeElasticity(requestParameter.getRequestElasticityParameter(), whsArtTimeline, false);
		} catch (Exception e) {
			responseElasticity = new ResponseElasticity(requestParameter.getRequestElasticityParameter().getWhsId(), requestParameter.getRequestElasticityParameter().getArtId());
			responseElasticity.setErrorMessage("Can't calculate elasticity:" + e.toString());
			LOG.error("Can't calculate elasticity:" + e.toString());
		}
		ResponseForecastAndElasticity result = new ResponseForecastAndElasticity(responseForecast, responseElasticity);
		return result;
	}
}
