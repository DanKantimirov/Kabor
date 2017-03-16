package ru.kabor.demand.prediction.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.apache.catalina.connector.Connector;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import ru.kabor.demand.prediction.entity.RequestForecastParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponceForecast;
import ru.kabor.demand.prediction.entity.WhsArtTimeline;
import ru.kabor.demand.prediction.r.RUtils;
import ru.kabor.demand.prediction.service.DataServiceException;
import ru.kabor.demand.prediction.utils.FORECAST_METHOD;
import ru.kabor.demand.prediction.utils.MultithreadingCallable;
import ru.kabor.demand.prediction.utils.SMOOTH_TYPE;
import ru.kabor.demand.prediction.utils.WhsArtTimelineBuilder;

@org.springframework.stereotype.Repository("dataRepository")
@Transactional
public class DataRepositoryImpl implements DataRepository{
	
	
	@Value("${sql.querytimeout}")
	private Integer queryTimeout;
	
	@Value("${parallel.countThreads}")
	private Integer countThreads;
	
    @Value("${storage.outputFolderLocation}")
    private String outputFolderLocation;

    @Value("${serverUser.contextPath}")
    private String contextPath;

    @Value("${serverUser.port}")
    private String port;
	
	@Autowired
	private NamedParameterJdbcTemplate namedparameterJdbcTemplate;
	
	@Autowired
	private RUtils rUtils;
	
	@Value("${storage.inputFolderLocation}")
	private Path inputFolderLocation; 
	
	@Autowired
	private EmbeddedWebApplicationContext appContext;
	
	private String fileSeparator = System.getProperty("file.separator");
	
	private static final Logger LOG = LoggerFactory.getLogger(DataRepositoryImpl.class);
	
	public String baseUrl;
	
	@PostConstruct
	private void init() throws UnknownHostException{
		Connector connector = ((TomcatEmbeddedServletContainer) appContext.getEmbeddedServletContainer()).getTomcat().getConnector();
		String scheme = connector.getScheme();
		String ip = InetAddress.getLocalHost().getHostAddress();
		int port = connector.getPort();
		String contextPath = appContext.getServletContext().getContextPath();
		baseUrl = scheme + "://" + ip + ":" + port + contextPath;
	}
	
	@Override
	@Transactional
	public SqlRowSet getSalesMultiple(RequestForecastParameterMultiple forecastParameters) throws DataServiceException {
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
		
		query = "select whs_id, f.art_id, sale_qnty, rest_qnty,day_id  from v_sales_rest f  where f.request_id = :requestIdParam and f.whs_id in(:listWhsIdParam)  and f.art_id in (:listArtIdParam) and f.day_id between date_format(:dayStartParam ,'%Y-%m-%d') and date_format(:dayFinishParam, '%Y-%m-%d')  order by f.whs_id, f.art_id, f.day_id";
		rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		return rowSet;
	}
	
	
	@Override
	@Transactional
	public SqlRowSet getSales(RequestForecastParameterSingle forecastParameters) throws DataServiceException {
		Integer requestId = forecastParameters.getRequestId();
		Integer whsId = forecastParameters.getWhsId();
		Integer artId = forecastParameters.getArtId();
		String trainingStart = forecastParameters.getTrainingStart();
		String trainingEnd = forecastParameters.getTrainingEnd();
		
		//Check existence that request_id, whs_id and art_id
		
		Map<String, Object> namedParameters = new HashMap<>();
		namedParameters.put("requestIdParam", requestId);
		namedParameters.put("whsIdParam", whsId);
		namedParameters.put("artIdParam", artId);
		namedParameters.put("dayStartParam", trainingStart);
		namedParameters.put("dayFinishParam", trainingEnd);
		
		String query = "select count(*) from v_sales_rest f where f.request_id = :requestIdParam and  f.whs_id = :whsIdParam  and f.art_id = :artIdParam and f.art_id = :artIdParam and f.day_id between date_format(:dayStartParam , '%Y-%m-%d') and date_format(:dayFinishParam , '%Y-%m-%d')";
		SqlRowSet rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		Integer countRecords = 0;
		if(rowSet.next()){
			countRecords = rowSet.getInt(1);
		}
		if(countRecords.equals(0)){
			throw new DataServiceException("Can't find any records for that request_id:" + requestId + " whs_id:" + whsId + " art_id:" + artId);
		}
		
		query = "select whs_id, f.art_id, sale_qnty, rest_qnty,day_id from v_sales_rest f where f.request_id = :requestIdParam and  f.whs_id = :whsIdParam and f.art_id = :artIdParam and f.day_id between date_format(:dayStartParam , '%Y-%m-%d') and date_format(:dayFinishParam , '%Y-%m-%d') order by f.art_id, f.day_id";
		rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		return rowSet;
	}
	
	@Override
	public List<ResponceForecast> getForecastMultiple(RequestForecastParameterMultiple forecastParameters, SqlRowSet salesRowSet) throws DataServiceException {
		List<ResponceForecast> result = new ArrayList<>();
		List<WhsArtTimeline> whsArtTimelineList = WhsArtTimelineBuilder.buildWhsArtTimelineList(salesRowSet);
		//create parallel call
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
			MultithreadingCallable forecastCallable = new MultithreadingCallable(forecastParameter, whsArtTimelineList.get(i),this.rUtils);
			Future<ResponceForecast> future = executorService.submit(forecastCallable);
			resultList.add(future);
		}
		executorService.shutdown();
		
		try {
			executorService.awaitTermination(200, TimeUnit.SECONDS);
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
	public ResponceForecast getForecast(RequestForecastParameterSingle forecastParameters, SqlRowSet salesRowSet) throws DataServiceException {
		ResponceForecast result = new ResponceForecast();
		WhsArtTimeline whsArtTimeline = WhsArtTimelineBuilder.buildWhsArtTimeline(salesRowSet);
		if(whsArtTimeline.getTimeMoments().size()==0){
			throw new DataServiceException("Can't find sales for that request");
		}
		try {
			result = rUtils.makePrediction(forecastParameters, whsArtTimeline);
		} catch (Exception e) {
			LOG.error("Getting forecast result exception: " + e.toString());
			throw new DataServiceException(e.toString());
		}
		return result;
	}

	@Override
	public String getForecastFile(ResponceForecast responceForecast) throws DataServiceException {
		List<ResponceForecast> responceForecastList = new ArrayList<>();
		responceForecastList.add(responceForecast);
		return this.getForecastFileMultiple(responceForecastList);
	}
	
	@Override
	public String getForecastFileMultiple(List<ResponceForecast> responceForecastList) throws DataServiceException {
		FileOutputStream out = null;
		Workbook book = null;
		String fullFilePath = "";
		String fileName = "";
		Integer rowSummarySheetNumber = 0;
		Integer rowPredictionSheetNumber = 0;

		try {
			book = new HSSFWorkbook();

			Sheet sheetSummary = book.createSheet("Summary");
			Row row = sheetSummary.createRow(0);
			Cell cell;
			// Header summary
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
			
			
			Sheet sheetPrediction = book.createSheet("Prediction");

			row = sheetPrediction.createRow(0);
			
			//Header prediction
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
			cell.setCellValue("isPrediction");
			
			// +++++++++++++++++Total row count++++++++++++++++++
			Integer predictionRowCount = responceForecastList.stream().mapToInt(e -> e.getTimeMomentsPrediction().size()).sum();

			if (predictionRowCount > 65000) {
				predictionRowCount = 65000;
			}

			Integer maxPredictionPerForecast = predictionRowCount / responceForecastList.size();
			Integer maxActualRowPeResponseForecast = (65500 - maxPredictionPerForecast) / responceForecastList.size();
			// +++++++++++++++++Main For+++++++++++++++++++++++++
			for (int k = 0; k < responceForecastList.size(); k++) {

				ResponceForecast responceForecast = responceForecastList.get(k);

				Integer printedActual = 0;
				Integer startI = responceForecast.getTimeMomentsActual().size() - maxActualRowPeResponseForecast;
				if (startI < 0) {
					startI = 0;
				}
				for (int i = startI; i <responceForecast.getTimeMomentsActual().size(); i++) {
					rowPredictionSheetNumber++;
					row = sheetPrediction.createRow(rowPredictionSheetNumber);
					cell = row.createCell(0);
					cell.setCellValue(responceForecast.getWhsId());
					cell = row.createCell(1);
					cell.setCellValue(responceForecast.getArtId());
					cell = row.createCell(2);
					cell.setCellValue(responceForecast.getTimeMomentsActual().get(i).getTimeMoment().toString());
					cell = row.createCell(3);
					cell.setCellValue(responceForecast.getTimeMomentsActual().get(i).getSalesQnty());
					cell = row.createCell(4);
					cell.setCellValue(responceForecast.getTimeMomentsSmoothed().get(i).getSalesQnty());

					cell = row.createCell(5);
					cell.setCellValue(0);
					printedActual++;
				}

				Integer printedForecast=0;
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
					cell.setCellValue(responceForecast.getTimeMomentsPrediction().get(i).getSalesQnty());

					cell = row.createCell(5);
					cell.setCellValue(1);
					printedForecast++;
				}
			}
			//+++++++++++++++++Main For+++++++++++++++++++++++++

			// Меняем размер столбца
			sheetPrediction.autoSizeColumn(0);
			sheetPrediction.autoSizeColumn(1);
			sheetPrediction.autoSizeColumn(2);
			sheetPrediction.autoSizeColumn(3);
			sheetPrediction.autoSizeColumn(4);
			sheetPrediction.autoSizeColumn(5);
			
			fileName = RandomStringUtils.randomAlphanumeric(32) + ".xls";;
			
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
	
	/** Getting user email (v_request.email) by requestId*/
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

	/** Getting response to user (v_request.response_text) by requestId*/
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
	/**
	 * Getting link to attachment for user
	 * (http://....v_request.attachment_path) by requestId
	 */
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
	/** Getting list of RequestForecastParameterSingle by requestId*/
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
	public Integer deleteRequestByResponseTimeBeforeMoment(Date dateBound) {
		Map<String, Object> namedParameters = new HashMap<>();
		namedParameters.put("dateBound", dateBound);
		String query = "delete from v_request where response_date_time <= :dateBound";
		return namedparameterJdbcTemplate.update(query, namedParameters);
	}
}
