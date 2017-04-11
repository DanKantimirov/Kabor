package ru.kabor.demand.prediction.service;

import static ru.kabor.demand.prediction.utils.ConstantUtils.PARSE_EXCEL_SALES_REST_LIST_SIZE;
import static ru.kabor.demand.prediction.utils.ExcelUtils.readValueFromXls;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.monitorjbl.xlsx.StreamingReader;

import ru.kabor.demand.prediction.entity.ElasticityParameter;
import ru.kabor.demand.prediction.entity.ForecastParameter;
import ru.kabor.demand.prediction.entity.Request;
import ru.kabor.demand.prediction.entity.ResponceElasticity;
import ru.kabor.demand.prediction.entity.ResponceForecast;
import ru.kabor.demand.prediction.entity.ResponceForecastAndElasticity;
import ru.kabor.demand.prediction.entity.SalesRest;
import ru.kabor.demand.prediction.repository.ElasticityParameterRepository;
import ru.kabor.demand.prediction.repository.ForecastParameterRepository;
import ru.kabor.demand.prediction.repository.RequestRepository;
import ru.kabor.demand.prediction.utils.ConstantUtils;
import ru.kabor.demand.prediction.utils.ExcelUtils;
import ru.kabor.demand.prediction.utils.FORECAST_METHOD;
import ru.kabor.demand.prediction.utils.SMOOTH_TYPE;
import ru.kabor.demand.prediction.utils.exceptions.InvalidHeaderException;

/** Implementation of RequestService */
@Service
public class RequestServiceImpl implements RequestService {

    private static final Logger LOG = LoggerFactory.getLogger(RequestServiceImpl.class);

    @Autowired
    RequestRepository requestRepository;
    
    @Autowired
    ForecastParameterRepository forecastParameterRepository;
    
    @Autowired
    ElasticityParameterRepository elasticityParameterRepository;

    @Autowired
    private DataService dataService;

    @Autowired
    private SalesRestService salesRestService;
    
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
	DateTimeFormatter simpleDateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public Request createRequest(Map<String, String[]> reqParams, MultipartFile file) throws DataServiceException {
        LOG.debug("prepare request for saving to db ");

        String fileName = dataService.putFileInInputStorage(file);
        
        Request request = new Request();
        request.setRequestType(reqParams.get("elasticityTypeInput")[0]);
        request.setDocumentPath(fileName);
        request.setEmail(reqParams.get("inputEmail")[0]);
        request.setStatus(ConstantUtils.REQUEST_ADDED);
        request.setSendDateTime(LocalDateTime.now());
        request = requestRepository.saveAndFlush(request);
        
        if(request.getRequestType().equals(ConstantUtils.REQUEST_TYPE_FORECAST) || request.getRequestType().equals(ConstantUtils.REQUEST_TYPE_FORECASTANDELASTICITY)){
            ForecastParameter forecastParameter = new ForecastParameter();
            String defaultSettingsInput = reqParams.get("defaultSettingsInput")[0];
            if(defaultSettingsInput!=null && defaultSettingsInput.equals("1")){
            	forecastParameter.setDuration(7);
            	forecastParameter.setForecast_method(FORECAST_METHOD.ARIMA_AUTO);
            	forecastParameter.setSmoothing_method(SMOOTH_TYPE.NO);
            } else{
            	Integer duration = Integer.valueOf(reqParams.get("predictionDaysInput")[0]);
            	FORECAST_METHOD method = FORECAST_METHOD.valueOf(reqParams.get("predictionMethod")[0]);
            	SMOOTH_TYPE smooth = SMOOTH_TYPE.valueOf(reqParams.get("useSmoothInput")[0]);
            	forecastParameter.setDuration(duration);
            	forecastParameter.setForecast_method(method);
            	forecastParameter.setSmoothing_method(smooth);
            }
            forecastParameter.setRequest(request);
            forecastParameterRepository.save(forecastParameter);
        }
        
        if(request.getRequestType().equals(ConstantUtils.REQUEST_TYPE_ELASTICITY) || request.getRequestType().equals(ConstantUtils.REQUEST_TYPE_FORECASTANDELASTICITY)){
        	ElasticityParameter elasticityParameter = new ElasticityParameter();
        	elasticityParameter.setRequest(request);
        	elasticityParameterRepository.save(elasticityParameter);
        }

        LOG.debug("new request successfully saved to db");

        return request;
    }

    @Override
    public Request addNewRequest(MultipartFile file, Map<String, String[]> reqParams)
            throws InvalidHeaderException, IOException, InvalidFormatException, DataServiceException {
        LOG.debug("processing new request. validate ans save");
        String fileName = file.getOriginalFilename();
        if(fileName.toLowerCase().contains(".xlsx")){
        	ExcelUtils.validateXLSXHeaders(file,reqParams.get("elasticityTypeInput")[0]);
        } else{
        	ExcelUtils.validateXLSHeaders(file,reqParams.get("elasticityTypeInput")[0]);
        }
        return createRequest(reqParams, file);
    }

    @Override
    public Request importRawRequest() throws IOException, InvalidFormatException {
        LOG.debug("ready for read request for parsing from db");
          Request request = requestRepository.findTop1ByStatus(ConstantUtils.REQUEST_ADDED);
        if (request != null) {
        	LOG.debug("Request "+request.getId()+" .Start importing record to db.");
            request.setStatus(ConstantUtils.REQUEST_HOLDED_BY_DATA_IMPORT);
            requestRepository.saveAndFlush(request);
			try {
				//+++++++++++++++++++++++++++++++ IMPORT SALES REST+++++++++++++++++++++++++++++++++++++++
				Path file = dataService.getFilePathFromInputStorage(request.getDocumentPath());
				String fileName = file.getFileName().toString();
				
				if(fileName.toLowerCase().contains(".xlsx")){
					//XLSX file
					StreamingReader reader = null;
    				try {
    					reader = StreamingReader.builder()
    					        .rowCacheSize(300)    // number of rows to keep in memory (defaults to 10)
    					        .bufferSize(6144)     // buffer size to use when reading InputStream to file (defaults to 1024)
    					        .sheetIndex(0) 
    					        .read(file.toFile());            // InputStream or File for XLSX file (required)
    					Iterator<Row> iterator =  reader.iterator();
    					int rowCounter = 0;
    					List<SalesRest> saleRestList = new ArrayList<>();
    					while (iterator.hasNext()) {
    						LOG.debug("Request "+request.getId()+ " .Processing workbook. row:"+ rowCounter);
    						Row row = iterator.next();
    						rowCounter++;
    						if (rowCounter == 1) {
    							continue;	// ignore header
    						}
    
    						SalesRest saleRest = new SalesRest();
    						saleRest.setRequest(request);
    						String whsId = null;
    						String artId = null;
    						String dayId = null;
    						String saleQnty = null;
    						String price = null;
    						
    						Integer currentCellColumn = 0;
    						for (Cell cellInRow : row) {
    							if(currentCellColumn==0){
    								whsId =  ExcelUtils.readCellWithoutFormulas(cellInRow,null);
    							}else if (currentCellColumn==1){
    								artId = ExcelUtils.readCellWithoutFormulas(cellInRow,null);
    							}else if (currentCellColumn==2){
    								dayId = ExcelUtils.readCellWithoutFormulas(cellInRow,simpleDateFormat);
    							}else if (currentCellColumn==3){
    								saleQnty = ExcelUtils.readCellWithoutFormulas(cellInRow,null);
    							}else if (currentCellColumn==4){
    								price = ExcelUtils.readCellWithoutFormulas(cellInRow,null);
    							}
    							currentCellColumn++;
    						}
    						
    						if(whsId==null || whsId.trim().equals("") || artId==null || artId.trim().equals("") || dayId==null || dayId.trim().equals("")){
    							continue;	//empty string
    						}
    						
    						saleRest.setWhsId(Integer.parseInt(whsId));
    						saleRest.setArtId(Integer.parseInt(artId));
    						saleRest.setDayId(LocalDate.parse(dayId, simpleDateTimeFormatter));
    						
    						if(saleQnty!=null && !saleQnty.equals("")){
    							saleRest.setSaleQnty(Double.parseDouble(saleQnty));
    						} else{
    							saleRest.setSaleQnty(0.0);
    						}
    						
    						if(price!=null && !price.equals("")){
    							saleRest.setPrice(Double.parseDouble(price));
    						} else {
    							saleRest.setPrice(0.0);
    						}
    						
    						saleRestList.add(saleRest);
    						if (rowCounter % PARSE_EXCEL_SALES_REST_LIST_SIZE == 0) {
    							LOG.debug("Request "+request.getId()+" .SalesRest batch ready. saving it to db");
    							salesRestService.storeBathSalesRest(saleRestList);
    							saleRestList.clear();
    						}
    					}
    					if (saleRestList.size() > 0) {
    						LOG.debug("Request "+request.getId()+" .SalesRest batch ready. saving it to db");
    						salesRestService.storeBathSalesRest(saleRestList);
    					}
    				} finally {
    					if (reader != null) {
    						reader.close();
    					}
    				}
				} else{
    				//XLS file			
    				Workbook workbook = WorkbookFactory.create(file.toFile());
    				try {
    					Sheet sheet = workbook.getSheetAt(0);
    					Iterator<Row> iterator = sheet.rowIterator();
    					int rowCounter = 0;
    					List<SalesRest> saleRestList = new ArrayList<>();
    					while (iterator.hasNext()) {
    						LOG.debug("Request "+request.getId()+ " .Processing workbook. row:"+ rowCounter);
    						Row row = iterator.next();
    						rowCounter++;
    						if (rowCounter == 1) {
    							continue;	// ignore header
    						}
    
    						SalesRest saleRest = new SalesRest();
    						saleRest.setRequest(request);
    						String whsId = readValueFromXls(workbook, row, 0);
    						String artId = readValueFromXls(workbook, row, 1);
    						String dayId = readValueFromXls(workbook, row, 2, simpleDateFormat);
    						String saleQnty = readValueFromXls(workbook, row, 3);
    						String price = readValueFromXls(workbook, row, 4);
    						if(whsId==null || whsId.trim().equals("") || artId==null || artId.trim().equals("") || dayId==null || dayId.trim().equals("")){
    							continue;	//empty string
    						}
    						
    						saleRest.setWhsId(Integer.parseInt(whsId));
    						saleRest.setArtId(Integer.parseInt(artId));
    						saleRest.setDayId(LocalDate.parse(dayId, simpleDateTimeFormatter));
    						
    						if(saleQnty!=null && !saleQnty.equals("")){
    							saleRest.setSaleQnty(Double.parseDouble(saleQnty));
    						} else{
    							saleRest.setSaleQnty(0.0);
    						}
    						
    						if(price!=null && !price.equals("")){
    							saleRest.setPrice(Double.parseDouble(price));
    						} else {
    							saleRest.setPrice(0.0);
    						}
    						
    						saleRestList.add(saleRest);
    						if (rowCounter % PARSE_EXCEL_SALES_REST_LIST_SIZE == 0) {
    							LOG.debug("Request "+request.getId()+" .SalesRest batch ready. saving it to db");
    							salesRestService.storeBathSalesRest(saleRestList);
    							saleRestList.clear();
    						}
    					}
    					if (saleRestList.size() > 0) {
    						LOG.debug("Request "+request.getId()+" .SalesRest batch ready. saving it to db");
    						salesRestService.storeBathSalesRest(saleRestList);
    					}
    				} finally {
    					if(workbook!=null){
    						workbook.close();
    					}
    				}
				}
				
				//--------------------------------- IMPORT SALES REST ----------------------------------------
                request.setStatus(ConstantUtils.REQUEST_DATA_IMPORTED);
                requestRepository.saveAndFlush(request);
                LOG.debug("Request "+request.getId()+" is successfully saved to db.");
                return request;
                
			} catch (Exception exception) {
				LOG.warn("Request " + request.getId() + " .Exception has occurred while importing records to database:" + exception.toString());
				request.setStatus(ConstantUtils.REQUEST_DATA_IMPORT_ERROR);
				request.setResponseText(exception.toString());
				requestRepository.saveAndFlush(request);
				throw exception;
			}
        }
        return null;
    }

	@Override
	public String makeRequestPrediction(Integer requestId) throws DataServiceException {
		LOG.info("Request " + requestId + " . Ready for making forecast");
		Request request = requestRepository.findOne(requestId);
		request.setStatus(ConstantUtils.REQUEST_HOLDED_BY_FORECASTING);
		requestRepository.saveAndFlush(request);

		List<ResponceForecast> forecastResponseList = null;
		String filePath = null;
		
		try {
			forecastResponseList = dataService.getForecastExcelMode(requestId);
		} catch (DataServiceException exception) {
			LOG.warn("Request " + request.getId() + " .Exception has occurred while making forecast:" + exception.toString());
			request.setStatus(ConstantUtils.REQUEST_FORECAST_ERROR);
			request.setResponseText(exception.toString());
			requestRepository.saveAndFlush(request);
			throw exception;
		}
		
		try {
			filePath = dataService.createForecastResultFileExcelMode(forecastResponseList);
		} catch (DataServiceException exception) {
			LOG.warn("Request " + request.getId() + " .Exception has occurred while making excel file:" + exception.toString());
			request.setStatus(ConstantUtils.REQUEST_FORECAST_ERROR);
			request.setResponseText(exception.toString());
			requestRepository.saveAndFlush(request);
			throw exception;
		}
		
		request.setAttachmentPath(filePath);
		request.setStatus(ConstantUtils.REQUEST_FORECAST_COMPLITED);
		request.setResponseText("Forecasting success");
		requestRepository.saveAndFlush(request);
		LOG.info("Request " + requestId + " .Forecasting completed.");
		return filePath;
	}
	
	@Override
	public String makeRequestElasticity(Integer requestId) throws DataServiceException {
		LOG.info("Request " + requestId + " . Ready for calculating elasticity");
		Request request = requestRepository.findOne(requestId);
		request.setStatus(ConstantUtils.REQUEST_HOLDED_BY_CALCULATING_ELASTICITY);
		requestRepository.saveAndFlush(request);

		List<ResponceElasticity> elasticityResponseList = null;
		String filePath = null;
		
		try {
			elasticityResponseList = dataService.getElasticitytExcelMode(requestId);
		} catch (DataServiceException exception) {
			LOG.warn("Request " + request.getId() + " .Exception has occurred while making forecast:" + exception.toString());
			request.setStatus(ConstantUtils.REQUEST_CALCULATING_ELASTICITY_ERROR);
			request.setResponseText(exception.toString());
			requestRepository.saveAndFlush(request);
			throw exception;
		}
		
		try {
			filePath = dataService.createElasticityResultFileExcelMode(elasticityResponseList);
		} catch (DataServiceException exception) {
			LOG.warn("Request " + request.getId() + " .Exception has occurred while making excel file:" + exception.toString());
			request.setStatus(ConstantUtils.REQUEST_CALCULATING_ELASTICITY_ERROR);
			request.setResponseText(exception.toString());
			requestRepository.saveAndFlush(request);
			throw exception;
		}
		
		request.setAttachmentPath(filePath);
		request.setStatus(ConstantUtils.REQUEST_CALCULATING_ELASTICITY_COMPLETED);
		request.setResponseText("Elasticity success");
		requestRepository.saveAndFlush(request);
		LOG.info("Request " + requestId + " .Calculating elasticity completed.");
		return filePath;
	}

	@Override
	public String makeRequestAndElasticityPrediction(Integer requestId) throws DataServiceException {
		LOG.info("Request " + requestId + " . Ready for making forecast and elasticity");
		Request request = requestRepository.findOne(requestId);
		request.setStatus(ConstantUtils.REQUEST_HOLDED_BY_FORECASTING_AND_ELASTICITY);
		requestRepository.saveAndFlush(request);

		List<ResponceForecastAndElasticity> forecastAndElasticityResponseList = null;
		String filePath = null;
		
		try {
			forecastAndElasticityResponseList = dataService.getForecastAndElasticitytExcelMode(requestId);
			
		} catch (DataServiceException exception) {
			LOG.warn("Request " + request.getId() + " .Exception has occurred while making forecast and elasticity:" + exception.toString());
			request.setStatus(ConstantUtils.REQUEST_FORECAST_ERROR);
			request.setResponseText(exception.toString());
			requestRepository.saveAndFlush(request);
			throw exception;
		}
		
		try {
			filePath = dataService.createForecastAndElasticityResultFileExcelMode(forecastAndElasticityResponseList);
		} catch (DataServiceException exception) {
			LOG.warn("Request " + request.getId() + " .Exception has occurred while making excel file:" + exception.toString());
			request.setStatus(ConstantUtils.REQUEST_FORECAST_ERROR);
			request.setResponseText(exception.toString());
			requestRepository.saveAndFlush(request);
			throw exception;
		}
		request.setAttachmentPath(filePath);
		request.setStatus(ConstantUtils.REQUEST_FORECAST_COMPLITED);
		request.setResponseText("Forecasting and elasticity success");
		requestRepository.saveAndFlush(request);
		LOG.info("Request " + requestId + " .Forecast and elasticity completed.");
		return filePath;
	}
}
