package ru.kabor.demand.prediction.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.naming.TimeLimitExceededException;
import javax.transaction.Transactional;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import ru.kabor.demand.prediction.entity.RequestForecastParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponceForecast;
import ru.kabor.demand.prediction.entity.WhsArtTimeline;
import ru.kabor.demand.prediction.r.RUtils;
import ru.kabor.demand.prediction.utils.MultithreadingCallable;
import ru.kabor.demand.prediction.utils.WhsArtTimelineBuilder;

@org.springframework.stereotype.Repository("dataRespitory")
@Transactional
public class DataRepositoryImplementation implements DataRepository{
	
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
	
	private String fileSeparator = System.getProperty("file.separator");
	
	@PostConstruct
	public void initIt() throws Exception {
		
	}
	
	@Override
	@Transactional
	public SqlRowSet getSalesMultiple(RequestForecastParameterMultiple forecastParameters) {
		
		String[] whsSplitted = forecastParameters.getWhsIdBulk().split(";");
		String[] artSplitted = forecastParameters.getArtBulk().split(";");
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
		
		String query = "select whs_id, f.art_id, sale_qnty, rest_qnty,day_id  from v_sales_rest f  where f.whs_id in(:listWhsIdParam)  and f.art_id in (:listArtIdParam) and f.day_id between date_format(:dayStartParam ,'%Y-%m-%d') and date_format(:dayFinishParam, '%Y-%m-%d')  order by f.whs_id, f.art_id, f.day_id";
		
		Map<String, Object> namedParameters = new HashMap<>();
		namedParameters.put("listWhsIdParam", whsList);
		namedParameters.put("listArtIdParam", artList);
		namedParameters.put("dayStartParam", forecastParameters.getTrainingStart());
		namedParameters.put("dayFinishParam", forecastParameters.getTrainingEnd());

		SqlRowSet rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		return rowSet;
	}
	
	
	@Override
	@Transactional
	public SqlRowSet getSales(RequestForecastParameterSingle forecastParameters) {
		Integer whsId = forecastParameters.getWhsId();
		Integer artId = forecastParameters.getArtId();
		String trainingStart = forecastParameters.getTrainingStart();
		String trainingEnd = forecastParameters.getTrainingEnd();
		
		Map<String, Object> namedParameters = new HashMap<>();
		namedParameters.put("whsIdParam", whsId);
		namedParameters.put("artIdParam", artId);
		namedParameters.put("dayStartParam", trainingStart);
		namedParameters.put("dayFinishParam", trainingEnd);
		
		String query = "select whs_id, f.art_id, sale_qnty, rest_qnty,day_id from v_sales_rest f where f.whs_id = :whsIdParam  and f.art_id = :artIdParam and f.day_id between date_format(:dayStartParam , '%Y-%m-%d') and date_format(:dayFinishParam , '%Y-%m-%d') order by f.art_id, f.day_id";
		SqlRowSet rowSet = namedparameterJdbcTemplate.queryForRowSet(query, namedParameters);
		return rowSet;
	}
	
	@Override
	public List<ResponceForecast> getForecastMultiple(RequestForecastParameterMultiple forecastParameters, SqlRowSet salesRowSet) throws Exception {
		List<ResponceForecast> result = new ArrayList<>();
		List<WhsArtTimeline> whsArtTimelineList = WhsArtTimelineBuilder.buildWhsArtTimelineList(salesRowSet);
		//create parallel call
		ExecutorService executorService = Executors.newFixedThreadPool(this.countThreads);
		List<Future<ResponceForecast>> resultList = new ArrayList<Future<ResponceForecast>>();
		
		for (int i = 0; i < whsArtTimelineList.size(); i++) {
			RequestForecastParameterSingle forecastParameter = new RequestForecastParameterSingle(
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
			throw new TimeLimitExceededException("Из R долго не поступают ответы");
		}
		
		for(Future<ResponceForecast> future: resultList){
			ResponceForecast responce =  future.get();
			result.add(responce);
		}
		
		return result;
	}
	

	
	@Override
	public ResponceForecast getForecast(RequestForecastParameterSingle forecastParameters, SqlRowSet salesRowSet) throws Exception {
		ResponceForecast result = new ResponceForecast();
		WhsArtTimeline whsArtTimeline = WhsArtTimelineBuilder.buildWhsArtTimeline(salesRowSet);
		result = rUtils.makePrediction(forecastParameters, whsArtTimeline);
		return result;
	}

	@Override
	public String getForecastFile(ResponceForecast responceForecast) throws Exception {
		FileOutputStream out = null;
		Workbook book = null;
		String fullFilePath = "";
		String fileName = "";
		Integer rowNumber = 0;

		try {
			book = new HSSFWorkbook();
			Sheet sheet = book.createSheet("Prediction");

			Row row = sheet.createRow(0);
			
			//Header
			Cell cell = row.createCell(0);
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
			
			for (int i = 0; i < responceForecast.getTimeMomentsActual().size(); i++) {
				rowNumber++;
				row = sheet.createRow(rowNumber);
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
			}

			for (int i = 0; i < responceForecast.getTimeMomentsPrediction().size(); i++) {
				rowNumber++;
				row = sheet.createRow(rowNumber);
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
			}

			// Меняем размер столбца
			sheet.autoSizeColumn(0);
			sheet.autoSizeColumn(1);
			sheet.autoSizeColumn(2);
			sheet.autoSizeColumn(3);
			sheet.autoSizeColumn(4);
			sheet.autoSizeColumn(5);
			fileName = responceForecast.getWhsId() + "_" + responceForecast.getArtId() + ".xls";
			fullFilePath = outputFolderLocation + fileSeparator +fileName;
			
			out = new FileOutputStream(new File(fullFilePath),false);
			
			book.write(out);
		} catch (Exception e) {
			throw new Exception("Can't build excel file" + e);
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
	
	@Override
	public String getForecastFileMultiple(List<ResponceForecast> responceForecastList) throws Exception {
		FileOutputStream out = null;
		Workbook book = null;
		String fullFilePath = "";
		String fileName = "";
		Integer rowNumber = 0;

		try {
			book = new HSSFWorkbook();
			Sheet sheet = book.createSheet("Prediction");

			Row row = sheet.createRow(0);
			
			//Header
			Cell cell = row.createCell(0);
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
			
			//+++++++++++++++++Main For+++++++++++++++++++++++++
			for(int k=0;k<responceForecastList.size();k++ ){
				ResponceForecast responceForecast= responceForecastList.get(k);
			
				for (int i = 0; i < responceForecast.getTimeMomentsActual().size(); i++) {
					rowNumber++;
					row = sheet.createRow(rowNumber);
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
				}
	
				for (int i = 0; i < responceForecast.getTimeMomentsPrediction().size(); i++) {
					rowNumber++;
					row = sheet.createRow(rowNumber);
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
				}
			
			}
			//+++++++++++++++++Main For+++++++++++++++++++++++++

			// Меняем размер столбца
			sheet.autoSizeColumn(0);
			sheet.autoSizeColumn(1);
			sheet.autoSizeColumn(2);
			sheet.autoSizeColumn(3);
			sheet.autoSizeColumn(4);
			sheet.autoSizeColumn(5);
			
			int randomNum = ThreadLocalRandom.current().nextInt(1, 10000000);
			
			fileName = randomNum + ".xls";
			fullFilePath = outputFolderLocation + fileSeparator + fileName;
			
			out = new FileOutputStream(new File(fullFilePath),false);
			book.write(out);
		} catch (Exception e) {
			throw new Exception("Can't build excel file" + e);
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
	
	@Override
	/** Get Excel file name of untreated document*/
	public String getAvailableDocument() {
		//TODO: get available document
		return "Dummy document";
	}
}
