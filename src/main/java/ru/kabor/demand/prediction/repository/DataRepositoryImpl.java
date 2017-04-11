package ru.kabor.demand.prediction.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.apache.catalina.connector.Connector;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import ru.kabor.demand.prediction.entity.RequestElasticityParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestElasticityParameterSingle;
import ru.kabor.demand.prediction.entity.RequestForecastAndElasticityParameterSingle;
import ru.kabor.demand.prediction.entity.RequestForecastParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponceElasticity;
import ru.kabor.demand.prediction.entity.ResponceForecast;
import ru.kabor.demand.prediction.entity.ResponceForecastAndElasticity;
import ru.kabor.demand.prediction.entity.TimeMomentDescription;
import ru.kabor.demand.prediction.entity.WhsArtTimeline;
import ru.kabor.demand.prediction.r.RUtils;
import ru.kabor.demand.prediction.service.DataServiceException;
import ru.kabor.demand.prediction.utils.FORECAST_METHOD;
import ru.kabor.demand.prediction.utils.MultithreadingElasticityCallable;
import ru.kabor.demand.prediction.utils.MultithreadingForecastCallable;
import ru.kabor.demand.prediction.utils.SMOOTH_TYPE;
import ru.kabor.demand.prediction.utils.WhsArtTimelineBuilder;

/** Implementation for DataRepository*/
@org.springframework.stereotype.Repository("dataRepository")
@Transactional
public class DataRepositoryImpl implements DataRepository{
	
	@Value("${sql.querytimeout}")
	private Integer queryTimeout;
	
	@Value("${parallel.countThreads}")
	private Integer countThreads;
	
    @Value("${storage.outputFolderLocation}")
    private String outputFolderLocation;
	
	@Autowired
	private NamedParameterJdbcTemplate namedparameterJdbcTemplate;
	
	@Autowired
	private RUtils rUtils;
	
	@Value("${storage.inputFolderLocation}")
	private Path inputFolderLocation;
	
	@Autowired
	private EmbeddedWebApplicationContext appContext;
	
	@Value("${serverUser.domainName}")
	private String domainName;
	
	@Value("${serverUser.useDomainName}")
	private Boolean useDomainName;
	
	DecimalFormat decimalFormat = new DecimalFormat("#.##");
	
	private String fileSeparator = System.getProperty("file.separator");
	
	private static final Logger LOG = LoggerFactory.getLogger(DataRepositoryImpl.class);
	
	public String baseUrl;
	
	private Integer maxAvaitTermination = 20000;
	
	@PostConstruct
	private void init() throws UnknownHostException{
		Connector connector = ((TomcatEmbeddedServletContainer) appContext.getEmbeddedServletContainer()).getTomcat().getConnector();
		String scheme = connector.getScheme();
		String domain = InetAddress.getLocalHost().getHostAddress();
		if (useDomainName != null && useDomainName == true && domainName != null && !domainName.trim().equals("")) {
			domain = domainName;
		}
		int port = connector.getPort();
		String contextPath = appContext.getServletContext().getContextPath();
		baseUrl = scheme + "://" + domain + ":" + port + contextPath;
		LOG.info(baseUrl);
	}
	
	@Override
	@Transactional
	public SqlRowSet getSalesMultipleWithPrices(RequestForecastParameterMultiple forecastParameters) throws DataServiceException {
		Integer requestId = forecastParameters.getRequestId();
		String[] whsSplitted = forecastParameters.getWhsIdBulk().split(";");
		String[] artSplitted = forecastParameters.getArtIdBulk().split(";");
		List<Integer> whsList = new ArrayList<>();
		List<Integer> artList = new ArrayList<>();
		
		//formatting
		for(int i=0;i<whsSplitted.length;i++){
			Integer whsId = -1;
			try{whsId = new Integer(whsSplitted[i]);} catch(NumberFormatException e){}
			whsList.add(whsId);
		}
		
		for(int i=0;i<artSplitted.length;i++){
			Integer artId = -1;
			try{artId = new Integer(artSplitted[i]);} catch(NumberFormatException e){}
			artList.add(artId);
		}
		
		Map<String, Object> namedParameters = new HashMap<>();
		namedParameters.put("requestIdParam", requestId);
		namedParameters.put("listWhsIdParam", whsList);
		namedParameters.put("listArtIdParam", artList);
		namedParameters.put("dayStartParam", forecastParameters.getTrainingStart());
		namedParameters.put("dayFinishParam", forecastParameters.getTrainingEnd());
		
		String query = "select count(*) from v_sales_rest f  where f.whs_id in(:listWhsIdParam)  and f.art_id in (:listArtIdParam) and f.day_id between date_format(:dayStartParam ,'%Y-%m-%d') and date_format(:dayFinishParam, '%Y-%m-%d')";
		SqlRowSet rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		Integer countRecords = 0;
		
		if(rowSet.next()){
			countRecords = rowSet.getInt(1);
		}
		
		if(countRecords.equals(0)){
			throw new DataServiceException("Can't find any records for that request_id:" + requestId + " whs_id_bulk:" + forecastParameters.getWhsIdBulk() + " art_id_bulk:" + forecastParameters.getArtIdBulk());
		}
		
		query = "select whs_id, f.art_id, sale_qnty, rest_qnty, day_id, price from v_sales_rest f where f.request_id = :requestIdParam and f.whs_id in(:listWhsIdParam)  and f.art_id in (:listArtIdParam) and f.day_id between date_format(:dayStartParam ,'%Y-%m-%d') and date_format(:dayFinishParam, '%Y-%m-%d')  order by f.whs_id, f.art_id, f.day_id";
		rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		return rowSet;
	}
	
	@Override
	@Transactional
	public SqlRowSet getSalesMultipleWithPrices(RequestElasticityParameterMultiple elasticityParameterMultiple) throws DataServiceException {
		Integer requestId = elasticityParameterMultiple.getRequestId();
		String[] whsSplitted = elasticityParameterMultiple.getWhsIdBulk().split(";");
		String[] artSplitted = elasticityParameterMultiple.getArtIdBulk().split(";");
		List<Integer> whsList = new ArrayList<>();
		List<Integer> artList = new ArrayList<>();
		
		//formatting
		for(int i=0;i<whsSplitted.length;i++){
			Integer whsId = -1;
			try{whsId = new Integer(whsSplitted[i]);} catch(NumberFormatException e){}
			whsList.add(whsId);
		}
		
		for(int i=0;i<artSplitted.length;i++){
			Integer artId = -1;
			try{artId = new Integer(artSplitted[i]);} catch(NumberFormatException e){}
			artList.add(artId);
		}
		
		Map<String, Object> namedParameters = new HashMap<>();
		namedParameters.put("requestIdParam", requestId);
		namedParameters.put("listWhsIdParam", whsList);
		namedParameters.put("listArtIdParam", artList);
		
		String query = "select count(*) from v_sales_rest f  where f.whs_id in(:listWhsIdParam)  and f.art_id in (:listArtIdParam) ";
		SqlRowSet rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		Integer countRecords = 0;
		
		if(rowSet.next()){
			countRecords = rowSet.getInt(1);
		}
		
		if(countRecords.equals(0)){
			throw new DataServiceException("Can't find any records for that request_id:" + requestId + " whs_id_bulk:" + elasticityParameterMultiple.getWhsIdBulk() + " art_id_bulk:" + elasticityParameterMultiple.getArtIdBulk());
		}
		
		query = "select whs_id, f.art_id, sale_qnty, rest_qnty, day_id, price from v_sales_rest f  where f.request_id = :requestIdParam and f.whs_id in(:listWhsIdParam)  and f.art_id in (:listArtIdParam) order by f.whs_id, f.art_id, f.day_id";
		rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		return rowSet;
	}
	
	@Override
	@Transactional
	public SqlRowSet getSalesWithPrices(RequestForecastParameterSingle forecastParameters) throws DataServiceException {
		Integer requestId = forecastParameters.getRequestId();
		Integer whsId = forecastParameters.getWhsId();
		Integer artId = forecastParameters.getArtId();
		String trainingStart = forecastParameters.getTrainingStart();
		String trainingEnd = forecastParameters.getTrainingEnd();
		
		Map<String, Object> namedParameters = new HashMap<>();
		namedParameters.put("requestIdParam", requestId);
		namedParameters.put("whsIdParam", whsId);
		namedParameters.put("artIdParam", artId);
		namedParameters.put("dayStartParam", trainingStart);
		namedParameters.put("dayFinishParam", trainingEnd);
		
		String query = "select count(*) from v_sales_rest f where f.request_id = :requestIdParam and  f.whs_id = :whsIdParam and f.art_id = :artIdParam and f.day_id between date_format(:dayStartParam , '%Y-%m-%d') and date_format(:dayFinishParam , '%Y-%m-%d')";
		SqlRowSet rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		Integer countRecords = 0;
		if(rowSet.next()){
			countRecords = rowSet.getInt(1);
		}
		if(countRecords.equals(0)){
			throw new DataServiceException("Can't find any records for that request_id:" + requestId + " whs_id:" + whsId + " art_id:" + artId);
		}
		
		query = "select whs_id, f.art_id, sale_qnty, rest_qnty, day_id, price from v_sales_rest f where f.request_id = :requestIdParam and  f.whs_id = :whsIdParam and f.art_id = :artIdParam and f.day_id between date_format(:dayStartParam , '%Y-%m-%d') and date_format(:dayFinishParam , '%Y-%m-%d') order by f.art_id, f.day_id";
		rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		return rowSet;
	}
	
	@Override
	public List<ResponceForecast> getForecastMultiple(RequestForecastParameterMultiple forecastParameters, SqlRowSet salesRowSet) throws DataServiceException {
		List<ResponceForecast> result = new ArrayList<>();
		List<WhsArtTimeline> whsArtTimelineList = WhsArtTimelineBuilder.buildWhsArtTimelineListWithPrices(salesRowSet);
		ExecutorService executorService = Executors.newFixedThreadPool(this.countThreads);
		List<Future<ResponceForecast>> resultList = new ArrayList<Future<ResponceForecast>>();
		
		for (int i = 0; i < whsArtTimelineList.size(); i++) {
			RequestForecastParameterSingle forecastParameter = new RequestForecastParameterSingle(
					forecastParameters.getRequestId(),
					whsArtTimelineList.get(i).getWhsId(), 
					whsArtTimelineList.get(i).getArtId(), 
					forecastParameters.getTrainingStart(), 
					forecastParameters.getTrainingEnd(), 
					forecastParameters.getForecastDuration(), 
					forecastParameters.getForecastMethod(), 
					forecastParameters.getSmoothType());
			MultithreadingForecastCallable forecastCallable = new MultithreadingForecastCallable(forecastParameter, whsArtTimelineList.get(i),this.rUtils);
			Future<ResponceForecast> future = executorService.submit(forecastCallable);
			resultList.add(future);
		}
		executorService.shutdown();
		
		try {
			executorService.awaitTermination(maxAvaitTermination, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new DataServiceException("R is not answerring too long");
		} finally{
			if(!executorService.isShutdown()){
				executorService.shutdownNow();
			}
		}
		
		for(Future<ResponceForecast> future: resultList){
			ResponceForecast responce;
			try {
				responce = future.get();
				result.add(responce);
			} catch (InterruptedException | ExecutionException e) {
				LOG.error("Getting forecast result exception: " + e.toString());
			}
		}
		return result;
	}
	
	@Override
	public List<ResponceElasticity> getElasticityMultiple(RequestElasticityParameterMultiple elasticityParameterMultiple, SqlRowSet salesRowSet) throws DataServiceException {
		List<ResponceElasticity> result = new ArrayList<>();
		List<WhsArtTimeline> whsArtTimelineList = WhsArtTimelineBuilder.buildWhsArtTimelineListWithPrices(salesRowSet);
		ExecutorService executorService = Executors.newFixedThreadPool(this.countThreads);
		List<Future<ResponceElasticity>> resultList = new ArrayList<Future<ResponceElasticity>>();
		
		for (int i = 0; i < whsArtTimelineList.size(); i++) {
			RequestElasticityParameterSingle elasticityParameter = new RequestElasticityParameterSingle(elasticityParameterMultiple.getRequestId(), whsArtTimelineList.get(i).getWhsId(), whsArtTimelineList.get(i).getArtId());
			MultithreadingElasticityCallable forecastCallable = new MultithreadingElasticityCallable(elasticityParameter, whsArtTimelineList.get(i),this.rUtils);
			Future<ResponceElasticity> future = executorService.submit(forecastCallable);
			resultList.add(future);
		}
		executorService.shutdown();
		
		try {
			executorService.awaitTermination(maxAvaitTermination, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new DataServiceException("R is not answerring too long");
		} finally{
			if(!executorService.isShutdown()){
				executorService.shutdownNow();
			}
		}
		
		for(Future<ResponceElasticity> future: resultList){
			ResponceElasticity responce;
			try {
				responce = future.get();
				result.add(responce);
			} catch (InterruptedException | ExecutionException e) {
				LOG.error("Getting elasticity result exception: " + e.toString());
			}
		}
		return result;
	}
	
	@Override
	public ResponceForecast getForecast(RequestForecastParameterSingle forecastParameters, SqlRowSet salesRowSet) throws DataServiceException {
		ResponceForecast result = new ResponceForecast();
		WhsArtTimeline whsArtTimeline = WhsArtTimelineBuilder.buildWhsArtTimelineWithPrices(salesRowSet);
		if(whsArtTimeline.getTimeMoments().size()==0){
			throw new DataServiceException("Can't find sales for that request");
		}
		try {
			rUtils.calculateWhsArtTimelineSlope(whsArtTimeline,forecastParameters.getSmoothType());			//calculate slope
			rUtils.calculateWhsArtTimelineTrendSeasonalAndRandom(whsArtTimeline);							//calculate trand,elasticity and remainder
			result = rUtils.makeForecast(forecastParameters, whsArtTimeline);
		} catch (Exception e) {
			LOG.error("Getting forecast result exception: " + e.toString());
			throw new DataServiceException(e.toString());
		}
		return result;
	}

	@Override
	public String createForecastResultFile(ResponceForecast responceForecast) throws DataServiceException {
		List<ResponceForecast> responceForecastList = new ArrayList<>();
		responceForecastList.add(responceForecast);
		return this.createForecastMultipleResultFile(responceForecastList);
	}
	
	@Override
	public String createForecastMultipleResultFile(List<ResponceForecast> responceForecastList) throws DataServiceException {
		FileOutputStream out = null;
		Workbook book = null;
		String fullFilePath = "";
		String fileName = "";
		try {
			book = new SXSSFWorkbook(1000);
			this.createForecastSheetWithSummary(book, responceForecastList);
			this.createForecastSheetWithPrediction(book, responceForecastList);
			fileName = RandomStringUtils.randomAlphanumeric(32) + ".xlsx";
			fullFilePath = outputFolderLocation + fileSeparator + fileName;
			out = new FileOutputStream(new File(fullFilePath), false);
			book.write(out);
		} catch (Exception e) {
			LOG.error("Can't build excel file.", e);
			throw new DataServiceException("Can't build excel file:" + e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (book != null) {
				try {
					book.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return fileName;
	}
	
	public void createForecastSheetWithPrediction(Workbook book, List<ResponceForecast> responceForecastList) {
		Integer rowPredictionSheetNumber = 0;
		Sheet sheetPrediction = book.createSheet("Prediction");
		Row row = sheetPrediction.createRow(0);
		Cell cell;
		cell = row.createCell(0);
		cell.setCellValue("whs_id");
		cell = row.createCell(1);
		cell.setCellValue("art_id");
		cell = row.createCell(2);
		cell.setCellValue("day_id");
		cell = row.createCell(3);
		cell.setCellValue("sales_qnty");
		cell = row.createCell(4);
		cell.setCellValue("smoothed sales_qnty");
		cell = row.createCell(5);
		cell.setCellValue("trand sales_qnty");
		cell = row.createCell(6);
		cell.setCellValue("seasonal sales_qnty");
		cell = row.createCell(7);
		cell.setCellValue("remainder sales_qnty");
		cell = row.createCell(8);
		cell.setCellValue("isPrediction");

		Integer predictionRowCount = responceForecastList.stream().mapToInt(e -> e.getTimeMomentsPrediction().size()).sum();
		if (predictionRowCount > 500000) {
			predictionRowCount = 500000;
		}

		Integer maxPredictionPerForecast = predictionRowCount / responceForecastList.size();
		Integer maxActualRowPeResponseForecast = (900000 - maxPredictionPerForecast) / responceForecastList.size();
		for (int k = 0; k < responceForecastList.size(); k++) {

			ResponceForecast responceForecast = responceForecastList.get(k);

			Integer printedActual = 0;
			Integer startI = responceForecast.getTimeMomentsActual().size() - maxActualRowPeResponseForecast;
			if (startI < 0) {
				startI = 0;
			}
			for (int i = startI; i < responceForecast.getTimeMomentsActual().size(); i++) {
				rowPredictionSheetNumber++;

				TimeMomentDescription timeMoment = responceForecast.getTimeMomentsActual().get(i);
				String dayId = timeMoment.getTimeMoment().toString();
				Double sales = timeMoment.getSales().getActualValue();
				Double salesSmooth = timeMoment.getSales().getSmoothedValue();
				Double salesTrand = timeMoment.getSales().getTrendValue();
				Double salesSeasonal = timeMoment.getSales().getSeasonalValue();
				Double salesRemainder = timeMoment.getSales().getRandomValue();

				row = sheetPrediction.createRow(rowPredictionSheetNumber);

				cell = row.createCell(0);
				cell.setCellValue(responceForecast.getWhsId());

				cell = row.createCell(1);
				cell.setCellValue(responceForecast.getArtId());

				cell = row.createCell(2);
				cell.setCellValue(dayId);

				cell = row.createCell(3);
				cell.setCellValue(sales);

				cell = row.createCell(4);
				if (salesSmooth != null) {
					cell.setCellValue(salesSmooth);
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(5);
				if (salesTrand != null) {
					cell.setCellValue(salesTrand);
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(6);
				if (salesSeasonal != null) {
					cell.setCellValue(salesSeasonal);
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(7);
				if (salesRemainder != null) {
					cell.setCellValue(salesRemainder);
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(8);
				cell.setCellValue(0);
				printedActual++;
			}

			Integer printedForecast = 0;
			for (int i = 0; i < responceForecast.getTimeMomentsPrediction().size(); i++) {
				if (printedForecast > maxPredictionPerForecast) {
					break;
				}
				rowPredictionSheetNumber++;
				row = sheetPrediction.createRow(rowPredictionSheetNumber);
				cell = row.createCell(0);
				cell.setCellValue(responceForecast.getWhsId());
				cell = row.createCell(1);
				cell.setCellValue(responceForecast.getArtId());
				cell = row.createCell(2);
				cell.setCellValue(responceForecast.getTimeMomentsPrediction().get(i).getTimeMoment().toString());
				cell = row.createCell(3);
				Double saleQnty = responceForecast.getTimeMomentsPrediction().get(i).getSales().getActualValue();

				if (saleQnty == null || saleQnty.equals(0)) {
					cell.setCellValue(0);
				} else if (saleQnty <= 0.45) {
					cell.setCellValue(0);
				} else {
					cell.setCellValue(Math.floor(saleQnty * 100) / 100);
				}

				cell = row.createCell(8);
				cell.setCellValue(1);
				printedForecast++;
			}
		}
	}

	public void createForecastSheetWithSummary(Workbook book, List<ResponceForecast> responceForecastList) {
		Integer rowSummarySheetNumber = 0;
		Sheet sheetSummary = book.createSheet("Forecast summary");
		Row row = sheetSummary.createRow(0);
		Cell cell;
		cell = row.createCell(0);
		cell.setCellValue("whs_id");
		cell = row.createCell(1);
		cell.setCellValue("art_id");
		cell = row.createCell(2);
		cell.setCellValue("status");
		cell = row.createCell(3);
		cell.setCellValue("info");

		for (int k = 0; k < responceForecastList.size(); k++) {
			rowSummarySheetNumber++;
			ResponceForecast responceForecast = responceForecastList.get(k);
			Boolean hasError = responceForecast.getHasError();
			String status = "";
			String info = "";

			if (hasError) {
				status = "error";
				info = responceForecast.getErrorMessage();
			} else {
				status = "success";
			}

			row = sheetSummary.createRow(rowSummarySheetNumber);
			cell = row.createCell(0);
			cell.setCellValue(responceForecast.getWhsId());
			cell = row.createCell(1);
			cell.setCellValue(responceForecast.getArtId());
			cell = row.createCell(2);
			cell.setCellValue(status);
			cell = row.createCell(3);
			cell.setCellValue(info);
		}
	}
	
	@Override
	@Transactional
	public String getEmailByRequestId(Long requestId) {
		Map<String, Object> namedParameters = new HashMap<>();
		namedParameters.put("requestId", requestId);
		String query = "select email from v_request where request_id=:requestId";
		SqlRowSet rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		Integer emailColumn =  rowSet.findColumn("email");
		if(rowSet.next()){
			return rowSet.getString(emailColumn);
		}else{
			return null;
		}
	}

	@Override
	@Transactional
	public String getResponseTextByRequestId(Long requestId) {
		Map<String, Object> namedParameters = new HashMap<>();
		namedParameters.put("requestId", requestId);
		String query = "select response_text from v_request where request_id=:requestId";
		SqlRowSet rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		Integer responseTextColumn =  rowSet.findColumn("response_text");
		if(rowSet.next()){
			return rowSet.getString(responseTextColumn);
		}else{
			return null;
		}
	}

	@Override
	@Transactional
	public String getAttachmentPathByRequestId(Long requestId) {
		Map<String, Object> namedParameters = new HashMap<>();
		namedParameters.put("requestId", requestId);
		String query = "select attachment_path from v_request where request_id=:requestId";
		SqlRowSet rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		Integer responseTextColumn = rowSet.findColumn("attachment_path");
		if (rowSet.next()) {
			String attachmentName = rowSet.getString(responseTextColumn);
			if (attachmentName != null && !attachmentName.trim().equals("")) {
				String result = this.baseUrl + "/excelMode/filesOutput/" + attachmentName;
				return result;
			}
			return null;
		}
		return null;
	}


	@Override
	@Transactional
	public List<RequestForecastParameterSingle> getRequestForecastParameterSingleList(Integer requestId) throws DataServiceException {
		
		List<RequestForecastParameterSingle> requestForecastParameterSingleList = new ArrayList<>();
		
		Integer duration = null;
		FORECAST_METHOD forecastMethod = null;
		SMOOTH_TYPE smoothingMethod = null;
		
		Map<String, Object> namedParameters = new HashMap<>();
		namedParameters.put("requestId", requestId);

		String query = "select duration, forecast_method, smoothing_method from v_forecast_parameter where request_id=:requestId";
		SqlRowSet rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		Integer durationColumn = rowSet.findColumn("duration");
		Integer forecastMethodColumn = rowSet.findColumn("forecast_method");
		Integer smoothingMethodColumn = rowSet.findColumn("smoothing_method");
		
		if (rowSet.next()) {
			duration = rowSet.getInt(durationColumn);
			forecastMethod = FORECAST_METHOD.valueOf(rowSet.getString(forecastMethodColumn));
			smoothingMethod = SMOOTH_TYPE.valueOf(rowSet.getString(smoothingMethodColumn));
		} else{
			throw new DataServiceException("Can find forecast parameter for reques:" + requestId);
		}
		
		query = "select whs_id, art_id, min(day_id) as trainingStart, max(day_id) as trainingEnd from v_sales_rest where request_id=:requestId group by whs_id, art_id";
		rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		Integer whsIdColumn = rowSet.findColumn("whs_id");
		Integer artIdColumn = rowSet.findColumn("art_id");
		Integer trainingStartColumn = rowSet.findColumn("trainingStart");
		Integer trainingEndColumn = rowSet.findColumn("trainingEnd");
		
		while(rowSet.next()){
			
			Integer whsId = rowSet.getInt(whsIdColumn);
			Integer artId = rowSet.getInt(artIdColumn);
			String trainingStart = rowSet.getString(trainingStartColumn);
			String trainingEnd = rowSet.getString(trainingEndColumn);
			
			RequestForecastParameterSingle requestForecastParameterSingle = new RequestForecastParameterSingle();
			requestForecastParameterSingle.setRequestId(requestId);
			requestForecastParameterSingle.setWhsId(whsId);
			requestForecastParameterSingle.setArtId(artId);
			requestForecastParameterSingle.setTrainingStart(trainingStart);
			requestForecastParameterSingle.setTrainingEnd(trainingEnd);
			requestForecastParameterSingle.setForecastDuration(duration);
			requestForecastParameterSingle.setForecastMethod(forecastMethod);
			requestForecastParameterSingle.setSmoothType(smoothingMethod);
			requestForecastParameterSingleList.add(requestForecastParameterSingle);
		}
		return requestForecastParameterSingleList;
	}

	@Override
	@Transactional
	public List<String> getAttachmentPathListByResponseTimeBeforeMoment(Date dateBound) {
		List<String> attachmentPathList = new ArrayList<>();
		Map<String, Object> namedParameters = new HashMap<>();
		namedParameters.put("dateBound", dateBound);
		
		String query = "select attachment_path from v_request where response_date_time <= :dateBound";
		SqlRowSet rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		Integer attachmentPathColumn = rowSet.findColumn("attachment_path");
		
		while(rowSet.next()){
			String attachmentPath = rowSet.getString(attachmentPathColumn);
			if(attachmentPath!=null && !attachmentPath.trim().equals("")){
				attachmentPathList.add(attachmentPath);
			}
		}
		return attachmentPathList;
	}

	@Override
	@Transactional
	public List<String> getDocumentPathListByResponseTimeBeforeMoment(Date dateBound) {
		List<String> documentPathList = new ArrayList<>();
		Map<String, Object> namedParameters = new HashMap<>();
		namedParameters.put("dateBound", dateBound);
		String query = "select document_path from v_request where response_date_time <= :dateBound";
		SqlRowSet rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		Integer documentPathColumn = rowSet.findColumn("document_path");
		
		while(rowSet.next()){
			String documentPath = rowSet.getString(documentPathColumn);
			if(documentPath!=null && !documentPath.trim().equals("")){
				documentPathList.add(documentPath);
			}
		}
		return documentPathList;
	}

	@Override
	@Transactional
	public Integer deleteRequestByResponseTimeBeforeMoment(Date dateBound) {
		Map<String, Object> namedParameters = new HashMap<>();
		namedParameters.put("dateBound", dateBound);
		String query = "delete from v_request where response_date_time <= :dateBound";
		return namedparameterJdbcTemplate.update(query, namedParameters);
	}

	@Override
	@Transactional
	public List<RequestElasticityParameterSingle> getRequestElasticityParameterSingleList(Integer requestId) {
		
		List<RequestElasticityParameterSingle> requestElasticityParameterSingleList = new ArrayList<>();
		Map<String, Object> namedParameters = new HashMap<>();
		namedParameters.put("requestId", requestId);
		String query = "select whs_id, art_id from v_sales_rest where request_id=:requestId group by whs_id, art_id";
		SqlRowSet rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		Integer whsIdColumn = rowSet.findColumn("whs_id");
		Integer artIdColumn = rowSet.findColumn("art_id");
		
		while(rowSet.next()){
			Integer whsId = rowSet.getInt(whsIdColumn);
			Integer artId = rowSet.getInt(artIdColumn);
			
			RequestElasticityParameterSingle requestElasticityParameterSingle = new RequestElasticityParameterSingle();
			requestElasticityParameterSingle.setArtId(artId);
			requestElasticityParameterSingle.setWhsId(whsId);
			requestElasticityParameterSingle.setRequestId(requestId);
			requestElasticityParameterSingleList.add(requestElasticityParameterSingle);
		}
		return requestElasticityParameterSingleList;
		
	}

	@Override
	@Transactional
	public SqlRowSet getSalesWithPrices(RequestElasticityParameterSingle elasticityParameter) throws DataServiceException {
		Integer requestId = elasticityParameter.getRequestId();
		Integer whsId = elasticityParameter.getWhsId();
		Integer artId = elasticityParameter.getArtId();
		Map<String, Object> namedParameters = new HashMap<>();
		namedParameters.put("requestIdParam", requestId);
		namedParameters.put("whsIdParam", whsId);
		namedParameters.put("artIdParam", artId);
		
		String query = "select count(*) from v_sales_rest f where f.request_id = :requestIdParam and  f.whs_id = :whsIdParam  and f.art_id = :artIdParam";
		SqlRowSet rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		Integer countRecords = 0;
		if(rowSet.next()){
			countRecords = rowSet.getInt(1);
		}
		if(countRecords.equals(0)){
			throw new DataServiceException("Can't find any records for that request_id:" + requestId + " whs_id:" + whsId + " art_id:" + artId);
		}
		
		query = "select whs_id, f.art_id, sale_qnty, rest_qnty, day_id, price from v_sales_rest f where f.request_id = :requestIdParam and  f.whs_id = :whsIdParam and f.art_id = :artIdParam order by f.art_id, f.day_id";
		rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		return rowSet;
	}

	@Override
	public ResponceElasticity getElasticity(RequestElasticityParameterSingle elasticityParameter, SqlRowSet salesRowSet) throws DataServiceException {
		ResponceElasticity result = new ResponceElasticity();
		WhsArtTimeline whsArtTimeline = WhsArtTimelineBuilder.buildWhsArtTimelineWithPrices(salesRowSet);
		if(whsArtTimeline.getTimeMoments().size()==0){
			throw new DataServiceException("Can't find sales for that request");
		}
		try {
			rUtils.calculateWhsArtTimelineSlope(whsArtTimeline,SMOOTH_TYPE.NO);				//Calculate slope
			rUtils.calculateWhsArtTimelineTrendSeasonalAndRandom(whsArtTimeline);			//Calculate trand,elasticity and remainder
			result = rUtils.makeElasticity(elasticityParameter, whsArtTimeline,true);		//Calculate elasticity
		} catch (Exception e) {
			LOG.error("Getting forecast result exception: " + e.toString());
			throw new DataServiceException(e.toString());
		}
		return result;
	}

	@Override
	public String createElasticityMultipleResultFile(List<ResponceElasticity> elasticityResponseList) throws DataServiceException {
		FileOutputStream out = null;
		Workbook book = null;
		String fullFilePath = "";
		String fileName = "";
		try {
			book = new SXSSFWorkbook(1000);
			this.createElasticitySheetWithFormulas(book, elasticityResponseList);
			this.createElasticitySheetWithDatas(book, elasticityResponseList);
			fileName = RandomStringUtils.randomAlphanumeric(32) + ".xlsx";;
			fullFilePath = outputFolderLocation + fileSeparator + fileName;
			out = new FileOutputStream(new File(fullFilePath),false);
			book.write(out);
		} catch (Exception e) {
			throw new DataServiceException("Can't build excel file:" + e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (book != null) {
				try {
					book.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return fileName;
	}
	
	public void createElasticitySheetWithFormulas(Workbook book, List<ResponceElasticity> elasticityResponseList) {
		Integer rowSummarySheetNumber = 0;
		Sheet sheetSummary = book.createSheet("Elasticity Summary");
		Row row = sheetSummary.createRow(0);
		Cell cell;
		cell = row.createCell(0);
		cell.setCellValue("whs_id");
		cell = row.createCell(1);
		cell.setCellValue("art_id");
		cell = row.createCell(2);
		cell.setCellValue("status");
		cell = row.createCell(3);
		cell.setCellValue("info");
		cell = row.createCell(4);
		cell.setCellValue("formula (Δ demand=)");
		cell = row.createCell(5);
		cell.setCellValue("error");

		for (int k = 0; k < elasticityResponseList.size(); k++) {
			rowSummarySheetNumber++;
			ResponceElasticity responceElasticity = elasticityResponseList.get(k);
			Boolean hasError = responceElasticity.getHasError();
			String status = "";
			String info = "";

			if (hasError) {
				status = "error";
				info = responceElasticity.getErrorMessage();
			} else if (responceElasticity.getFormula() == null) {
				status = "error";
				info = "too little data";
			} else {
				status = "success";
			}

			row = sheetSummary.createRow(rowSummarySheetNumber);
			cell = row.createCell(0);
			cell.setCellValue(responceElasticity.getWhsId());
			cell = row.createCell(1);
			cell.setCellValue(responceElasticity.getArtId());
			cell = row.createCell(2);
			cell.setCellValue(status);
			cell = row.createCell(3);
			cell.setCellValue(info);
			cell = row.createCell(4);
			if (responceElasticity.getFormula() != null) {
				cell.setCellValue(responceElasticity.getPrettyFormula());
			}
			cell = row.createCell(5);
			if (responceElasticity.getSigma() != null) {
				cell.setCellValue(responceElasticity.getSigma());
			}
		}
		return;
	}
	
	public void createElasticitySheetWithDatas(Workbook book, List<ResponceElasticity> elasticityResponseList){
		Integer rowDataSheetNumber = 0;
		Sheet sheetData = book.createSheet("Data");
		Cell cell;
		Row row = sheetData.createRow(0);
		cell = row.createCell(0);
		cell.setCellValue("whs_id");
		cell = row.createCell(1);
		cell.setCellValue("art_id");
		cell = row.createCell(2);
		cell.setCellValue("day_id");
		cell = row.createCell(3);
		cell.setCellValue("sales_qnty");
		cell = row.createCell(4);
		cell.setCellValue("trand sales_qnty");
		cell = row.createCell(5);
		cell.setCellValue("seasonal sales_qnty");
		cell = row.createCell(6);
		cell.setCellValue("remainder sales_qnty");
		for (int k = 0; k < elasticityResponseList.size(); k++) {
			ResponceElasticity responceElasticity = elasticityResponseList.get(k);
			List<TimeMomentDescription> timeMomentList = responceElasticity.getTimeMoments();
			if(timeMomentList!=null && timeMomentList.size()>0){
				for (int i = 0; i < timeMomentList.size(); i++) {
					rowDataSheetNumber++;
					
					TimeMomentDescription timeMoment = timeMomentList.get(i);
					String dayId = timeMoment.getTimeMoment().toString();
					Double sales = timeMoment.getSales().getActualValue();
					Double salesTrand = timeMoment.getSales().getTrendValue();
					Double salesSeasonal = timeMoment.getSales().getSeasonalValue();
					Double salesRemainder = timeMoment.getSales().getRandomValue();
					
					row = sheetData.createRow(rowDataSheetNumber);
					
					cell = row.createCell(0);
					cell.setCellValue(responceElasticity.getWhsId());
					
					cell = row.createCell(1);
					cell.setCellValue(responceElasticity.getArtId());
					
					cell = row.createCell(2);
					cell.setCellValue(dayId);
					
					cell = row.createCell(3);
					cell.setCellValue(sales);
					
					cell = row.createCell(4);
					if(salesTrand!=null){
						cell.setCellValue(salesTrand);
					} else{
						cell.setCellValue("");
					}
					
					cell = row.createCell(5);
					if(salesSeasonal!=null){
						cell.setCellValue(salesSeasonal);
					} else{
						cell.setCellValue("");
					}
					
					cell = row.createCell(6);
					if(salesRemainder!=null){
						cell.setCellValue(salesRemainder);
					} else{
						cell.setCellValue("");
					}
				}
			}
		}
		return;
	}

	@Override
	public ResponceForecastAndElasticity getForecastAndElasticity(RequestForecastAndElasticityParameterSingle requestParameter, SqlRowSet salesRowSet) throws DataServiceException {
		ResponceForecast responceForecast = new ResponceForecast();
		ResponceElasticity responceElasticity = new ResponceElasticity();
		ResponceForecastAndElasticity result = new ResponceForecastAndElasticity(responceForecast,responceElasticity);
		
		WhsArtTimeline whsArtTimeline = WhsArtTimelineBuilder.buildWhsArtTimelineWithPrices(salesRowSet);
		if(whsArtTimeline.getTimeMoments().size()==0){
			throw new DataServiceException("Can't find sales for that request");
		}
		try {
			rUtils.calculateWhsArtTimelineSlope(whsArtTimeline,requestParameter.getRequestForecastParameter().getSmoothType());		//calculate slope
			rUtils.calculateWhsArtTimelineTrendSeasonalAndRandom(whsArtTimeline);													//calculate trand,elasticity and remainder
			result = rUtils.makeForecastAndElasticity(requestParameter, whsArtTimeline);
		} catch (Exception e) {
			LOG.error("Getting forecast result exception: " + e.toString());
			throw new DataServiceException(e.toString());
		}
		return result;
	}

	@Override
	public List<RequestForecastAndElasticityParameterSingle> getRequestForecastAndElasticityParameterSingleList(Integer requestId) {
		List<RequestForecastAndElasticityParameterSingle> resultList = new ArrayList<>();
		List<RequestForecastParameterSingle> forecastParameterList = new ArrayList<>();
		
		try {
			forecastParameterList = this.getRequestForecastParameterSingleList(requestId);
		} catch (DataServiceException e) {
			LOG.error("Can't find forecast parameter for requestId:" + requestId);
			return resultList;
		}
		List<RequestElasticityParameterSingle> elasticityParameterList = this.getRequestElasticityParameterSingleList(requestId);
		
		for(RequestForecastParameterSingle requestForecast : forecastParameterList){
			RequestForecastAndElasticityParameterSingle forecastAndElasticity = new RequestForecastAndElasticityParameterSingle();
			forecastAndElasticity.setRequestForecastParameter(requestForecast);
			
			Optional<RequestElasticityParameterSingle> requestElasticityOptional = elasticityParameterList.stream().filter( 
					e->e.getRequestId().equals(requestForecast.getRequestId()) &&
					e.getArtId().equals(requestForecast.getArtId()) && 
					e.getWhsId().equals(requestForecast.getWhsId())
					).findFirst();
			if(requestElasticityOptional.isPresent()){
				forecastAndElasticity.setRequestElasticityParameter(requestElasticityOptional.get());
			}
			resultList.add(forecastAndElasticity);
		}
		return resultList;
	}

	@Override
	public String createForecastWithElasticityMultipleResultFile(List<ResponceForecastAndElasticity> forecastAndElasticityResponseList) throws DataServiceException {
		FileOutputStream out = null;
		Workbook book = null;
		String fullFilePath = "";
		String fileName = "";
		
		try {
			book = new SXSSFWorkbook(1000);
			List<ResponceForecast> responceForecastList = forecastAndElasticityResponseList.stream().map(e->e.getResponceForecast()).collect(Collectors.toList());
			List<ResponceElasticity> responceElasticityList = forecastAndElasticityResponseList.stream().map(e->e.getResponceElasticity()).collect(Collectors.toList());
			this.createForecastSheetWithSummary(book, responceForecastList);
			this.createForecastSheetWithPrediction(book, responceForecastList);
			this.createElasticitySheetWithFormulas(book, responceElasticityList);
			fileName = RandomStringUtils.randomAlphanumeric(32) + ".xlsx";;
			fullFilePath = outputFolderLocation + fileSeparator + fileName;
			out = new FileOutputStream(new File(fullFilePath),false);
			book.write(out);
		} catch (Exception e) {
			throw new DataServiceException("Can't build excel file:" + e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (book != null) {
				try {
					book.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return fileName;
	}
}
