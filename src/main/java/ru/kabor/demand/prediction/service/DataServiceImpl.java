package ru.kabor.demand.prediction.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import ru.kabor.demand.prediction.entity.RequestElasticityParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestElasticityParameterSingle;
import ru.kabor.demand.prediction.entity.RequestForecastAndElasticityParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastAndElasticityParameterSingle;
import ru.kabor.demand.prediction.entity.RequestForecastParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponseElasticity;
import ru.kabor.demand.prediction.entity.ResponseForecast;
import ru.kabor.demand.prediction.entity.ResponseForecastAndElasticity;
import ru.kabor.demand.prediction.repository.DataRepository;

/** Implementation for DataService */
@Component
public class DataServiceImpl implements DataService {

	private static final Logger LOG = LoggerFactory.getLogger(DataServiceImpl.class);

	@Autowired
	private DataRepository dataRepository;
	
	@Value("${storage.inputFolderLocation}")
	private Path inputFolderLocation;
	
	@Value("${storage.outputFolderLocation}")
	private Path outputFolderLocation;
	
	@Value("${parallel.countThreads}")
	private Integer countThreads;
	
	private Integer maxAvaitTermination = 20000;
	
	@PostConstruct
	private void postConstruct() throws DataServiceException{
		this.sturpUpFileStorages();
	}
	
	@Override
	public List<ResponseElasticity> getElasticityMultipleDatabaseMode(RequestElasticityParameterMultiple elasticityParameterMultiple) throws DataServiceException {
		String whsIdBulk = elasticityParameterMultiple.getWhsIdBulk();
		String artIdBulk = elasticityParameterMultiple.getArtIdBulk();
		
		if (whsIdBulk == null || whsIdBulk.trim().equals("")) {
			LOG.error("whs_id can't be empty" + elasticityParameterMultiple.toString());
			throw new DataServiceException("whs_id can't be empty");
		}
		if (artIdBulk == null || artIdBulk.trim().equals("")) {
			LOG.error("art_id can't be empty" + elasticityParameterMultiple.toString());
			throw new DataServiceException("art_id can't be empty");
		}
		
		whsIdBulk = whsIdBulk.trim();
		artIdBulk = artIdBulk.trim();
		
		Pattern patternForCheck = Pattern.compile("^[0-9;]+");  
        Matcher matcherForCheck = patternForCheck.matcher(whsIdBulk);  
		
		if(!matcherForCheck.matches()){
			throw new DataServiceException("only (0-9 or ;) are allowed for whs_id");
		}
		
        matcherForCheck = patternForCheck.matcher(artIdBulk);  
		
		if(!matcherForCheck.matches()){
			throw new DataServiceException("only (0-9 or ;) are allowed for art_id");
		}
		
		List<ResponseElasticity> elasticityList = null;
		try {
			SqlRowSet salesRowSet = dataRepository.getSalesMultipleWithPrices(elasticityParameterMultiple);
			elasticityList = dataRepository.getElasticityMultiple(elasticityParameterMultiple, salesRowSet);
		} catch (Exception e) {
			throw new DataServiceException(e.toString());
		}
		return elasticityList;
	}
	
	@Override
	public List<ResponseForecast> getForecastMultipleDatabaseMode(RequestForecastParameterMultiple forecastParameters) throws DataServiceException {
		String whsIdBulk = forecastParameters.getWhsIdBulk();
		String artIdBulk = forecastParameters.getArtIdBulk();
		String trainingStart = forecastParameters.getTrainingStart();
		String trainingEnd = forecastParameters.getTrainingEnd();
		
		if (whsIdBulk == null || whsIdBulk.trim().equals("")) {
			LOG.error("whs_id can't be empty" + forecastParameters.toString());
			throw new DataServiceException("whs_id can't be empty");
		}
		if (artIdBulk == null || artIdBulk.trim().equals("")) {
			LOG.error("art_id can't be empty" + forecastParameters.toString());
			throw new DataServiceException("art_id can't be empty");
		}
		whsIdBulk = whsIdBulk.trim();
		artIdBulk = artIdBulk.trim();
		
		Pattern patternForCheck = Pattern.compile("^[0-9;]+");  
        Matcher matcherForCheck = patternForCheck.matcher(whsIdBulk);  
		
		if(!matcherForCheck.matches()){
			throw new DataServiceException("only (0-9 or ;) are allowed for whs_id");
		}
		
        matcherForCheck = patternForCheck.matcher(artIdBulk);  
		
		if(!matcherForCheck.matches()){
			throw new DataServiceException("only (0-9 or ;) are allowed for art_id");
		}
		
		if (trainingStart == null || trainingStart.trim().equals("")) {
			LOG.error("start of training can't be empty" + forecastParameters.toString());
			throw new DataServiceException("start of training can't be empty");
		}
		if (trainingEnd == null || trainingEnd.trim().equals("")) {
			LOG.error("start of forecasting can't be empty" + forecastParameters.toString());
			throw new DataServiceException("start of forecasting can't be empty");
		}
		
		LocalDate startDate = LocalDate.parse(trainingStart);
		LocalDate endDate = LocalDate.parse(trainingEnd);
		
		if(!startDate.isBefore(endDate)){
			LOG.error("start of forecasting is before start of training" + forecastParameters.toString());
			throw new DataServiceException("start of forecasting is before start of training");
		}
		
		List<ResponseForecast> forecastList = null;
		try {
			SqlRowSet salesRowSet = dataRepository.getSalesMultipleWithPrices(forecastParameters);
			forecastList = dataRepository.getForecastMultiple(forecastParameters, salesRowSet);
		} catch (Exception e) {
			throw new DataServiceException(e.toString());
		}
		return forecastList;
	}
	
	@Override
	public List<ResponseElasticity> getElasticitytExcelMode(Integer requestId) throws DataServiceException {
		List<ResponseElasticity> responseList = new ArrayList<>();
		List<RequestElasticityParameterSingle> requestElasticityParameterSingleList = dataRepository.getRequestElasticityParameterSingleList(requestId);
		ExecutorService executorService = Executors.newFixedThreadPool(countThreads);
		List<Future<ResponseElasticity>> futureResponsetList = new ArrayList<Future<ResponseElasticity>>();
		
		for (int i = 0; i < requestElasticityParameterSingleList.size(); i++) {
			RequestElasticityParameterSingle elasticityParameter = requestElasticityParameterSingleList.get(i);
			Future<ResponseElasticity> futureResponse = executorService.submit(() -> {
				return this.getElasticitySingleDatabaseMode(elasticityParameter);
			});
			futureResponsetList.add(futureResponse);
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
		
		//Getting results from tasks
		for (Future<ResponseElasticity> futureResponse : futureResponsetList) {
			ResponseElasticity response;
			try {
				response = futureResponse.get();
				responseList.add(response);
				//Mark that request as completed
				requestElasticityParameterSingleList.removeIf(e->e.getArtId().equals(response.getArtId()) && e.getWhsId().equals(response.getWhsId()));
				
			} catch (Exception e) {
				LOG.error("Getting elasticity result exception: " + e.toString());
			}
		}
		
		//Making complete response
		for(RequestElasticityParameterSingle requestParameter : requestElasticityParameterSingleList){
			ResponseElasticity response = new ResponseElasticity(requestParameter.getWhsId(), requestParameter.getArtId());
			response.setErrorMessage("Couldn't calculate elasticity");
			responseList.add(response);
		}
		return responseList;
	}
	
	
	@Override
	public List<ResponseForecastAndElasticity> getForecastAndElasticitytExcelMode(Integer requestId) throws DataServiceException {
		List<ResponseForecastAndElasticity> responseList = new ArrayList<>();
		List<RequestForecastAndElasticityParameterSingle> requestForecastAndElasticityParameterSingleList = dataRepository.getRequestForecastAndElasticityParameterSingleList(requestId);
		ExecutorService executorService = Executors.newFixedThreadPool(countThreads);
		List<Future<ResponseForecastAndElasticity>> futureResponsetList = new ArrayList<Future<ResponseForecastAndElasticity>>();
		
		for (int i = 0; i < requestForecastAndElasticityParameterSingleList.size(); i++) {
			RequestForecastAndElasticityParameterSingle requestForecastAndElasticityParameter = requestForecastAndElasticityParameterSingleList.get(i);
			Future<ResponseForecastAndElasticity> futureResponse = executorService.submit(() -> {
				return this.getForecastAndElasticitySingleDatabaseMode(requestForecastAndElasticityParameter);
			});
			futureResponsetList.add(futureResponse);
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
		
		//Getting results from tasks
		for (Future<ResponseForecastAndElasticity> futureResponse : futureResponsetList) {
			ResponseForecastAndElasticity response;
			try {
				response = futureResponse.get();
				responseList.add(response);
				//Mark that request as completed
				requestForecastAndElasticityParameterSingleList.removeIf(e->e.getRequestForecastParameter().getArtId().equals(response.getResponseForecast().getArtId()) && e.getRequestForecastParameter().getWhsId().equals(response.getResponseForecast().getWhsId()));
			} catch (Exception e) {
				LOG.error("Getting elasticity result exception: " + e.toString());
			}
		}
		
		return responseList;
	}

	@Override
	public ResponseElasticity getElasticitySingleDatabaseMode(RequestElasticityParameterSingle elasticityParameter) {
		ResponseElasticity elasticity;
		
		Integer requestId = elasticityParameter.getRequestId();
		Integer whsId = elasticityParameter.getWhsId();
		Integer artId = elasticityParameter.getArtId();
		
		elasticity = new ResponseElasticity(whsId, artId);
		
		if (requestId == null) {
			LOG.error("request_id can't be empty" + elasticityParameter.toString());
			elasticity.setErrorMessage("request_id can't be empty");
			return elasticity;
		}

		if (whsId == null) {
			LOG.error("whs_id can't be empty" + elasticityParameter.toString());
			elasticity.setErrorMessage("whs_id can't be empty");
			return elasticity;
		}
		if (artId == null) {
			LOG.error("art_id can't be empty" + elasticityParameter.toString());
			elasticity.setErrorMessage("art_id can't be empty");
			return elasticity;
		}
		
		try {
			SqlRowSet salesRowSet = dataRepository.getSalesWithPrices(elasticityParameter);
			elasticity = dataRepository.getElasticity(elasticityParameter, salesRowSet);
			return elasticity;
		} catch (Exception e) {
			elasticity.setErrorMessage(e.toString());
			return elasticity;
		}
	}
	
	@Override
	public List<ResponseForecastAndElasticity> getForecastAndElasticityMultipleDatabaseMode(RequestForecastAndElasticityParameterMultiple forecastAndElasticityParameterMultiple) throws DataServiceException {
		String whsIdBulk = forecastAndElasticityParameterMultiple.getRequestForecastParameterMultiple().getWhsIdBulk();
		String artIdBulk = forecastAndElasticityParameterMultiple.getRequestForecastParameterMultiple().getArtIdBulk();
		
		if (whsIdBulk == null || whsIdBulk.trim().equals("")) {
			LOG.error("whs_id can't be empty" + forecastAndElasticityParameterMultiple.toString());
			throw new DataServiceException("whs_id can't be empty");
		}
		if (artIdBulk == null || artIdBulk.trim().equals("")) {
			LOG.error("art_id can't be empty" + forecastAndElasticityParameterMultiple.toString());
			throw new DataServiceException("art_id can't be empty");
		}
		
		whsIdBulk = whsIdBulk.trim();
		artIdBulk = artIdBulk.trim();
		
		Pattern patternForCheck = Pattern.compile("^[0-9;]+");  
        Matcher matcherForCheck = patternForCheck.matcher(whsIdBulk);  
		
		if(!matcherForCheck.matches()){
			throw new DataServiceException("only (0-9 or ;) are allowed for whs_id");
		}
		
        matcherForCheck = patternForCheck.matcher(artIdBulk);  
		
		if(!matcherForCheck.matches()){
			throw new DataServiceException("only (0-9 or ;) are allowed for art_id");
		}
		
		List<ResponseForecastAndElasticity> forecastAndElasticityList = null;
		try {
			SqlRowSet salesRowSet = dataRepository.getSalesMultipleWithPrices(forecastAndElasticityParameterMultiple.getRequestForecastParameterMultiple());
			forecastAndElasticityList = dataRepository.getForecastAndElasticityMultiple(forecastAndElasticityParameterMultiple, salesRowSet);
		} catch (Exception e) {
			throw new DataServiceException(e.toString());
		}
		return forecastAndElasticityList;
	}
	
	@Override
	public ResponseForecastAndElasticity getForecastAndElasticitySingleDatabaseMode(RequestForecastAndElasticityParameterSingle forecastAndElasticityParameters) {
		Integer requestId = forecastAndElasticityParameters.getRequestForecastParameter().getRequestId();
		Integer whsId = forecastAndElasticityParameters.getRequestForecastParameter().getWhsId();
		Integer artId = forecastAndElasticityParameters.getRequestForecastParameter().getArtId();
		String trainingStart = forecastAndElasticityParameters.getRequestForecastParameter().getTrainingStart();
		String trainingEnd = forecastAndElasticityParameters.getRequestForecastParameter().getTrainingEnd();
		
		ResponseElasticity elasticity = new ResponseElasticity(whsId, artId);
		ResponseForecast forecast = new ResponseForecast(whsId, artId);
		
		ResponseForecastAndElasticity result = new ResponseForecastAndElasticity(forecast, elasticity);
		
		if (requestId == null) {
			LOG.error("request_id can't be empty" + forecastAndElasticityParameters.getRequestForecastParameter().toString());
			forecast.setErrorMessage("request_id can't be empty");
			return result;
		}

		if (whsId == null) {
			LOG.error("whs_id can't be empty" + forecastAndElasticityParameters.getRequestForecastParameter().toString());
			forecast.setErrorMessage("whs_id can't be empty");
			return result;
		}
		if (artId == null) {
			LOG.error("art_id can't be empty" + forecastAndElasticityParameters.getRequestForecastParameter().toString());
			forecast.setErrorMessage("art_id can't be empty");
			return result;
		}
		if (trainingStart == null || trainingStart.trim().equals("")) {
			LOG.error("training_start can't be empty" + forecastAndElasticityParameters.getRequestForecastParameter().toString());
			forecast.setErrorMessage("training_start can't be empty");
			return result;
		}
		if (trainingEnd == null || trainingEnd.trim().equals("")) {
			LOG.error("training_end can't be empty" + forecastAndElasticityParameters.getRequestForecastParameter().toString());
			forecast.setErrorMessage("training_end can't be empty");
			return result;
		}
		
		LocalDate startDate = LocalDate.parse(trainingStart);
		LocalDate endDate = LocalDate.parse(trainingEnd);
		
		if(!startDate.isBefore(endDate)){
			LOG.error("Start date of forecasting is earlier than First date of analysis: " + forecastAndElasticityParameters.getRequestForecastParameter().toString());
			forecast.setErrorMessage("Start date of forecasting is earlier than First date of analysis");
			return result;
		}
		
		try {
			SqlRowSet salesRowSet = dataRepository.getSalesWithPrices(forecastAndElasticityParameters.getRequestForecastParameter());
			result = dataRepository.getForecastAndElasticity(forecastAndElasticityParameters, salesRowSet);
			return result;
		} catch (Exception e) {
			elasticity.setErrorMessage(e.toString());
			return result;
		}
	}
	

	@Override
	public ResponseForecast getForecastSingleDatabaseMode(RequestForecastParameterSingle forecastParameters){
		
		ResponseForecast forecast;
		
		Integer requestId = forecastParameters.getRequestId();
		Integer whsId = forecastParameters.getWhsId();
		Integer artId = forecastParameters.getArtId();
		String trainingStart = forecastParameters.getTrainingStart();
		String trainingEnd = forecastParameters.getTrainingEnd();
		forecast = new ResponseForecast(whsId, artId);
		
		if (requestId == null) {
			LOG.error("request_id can't be empty" + forecastParameters.toString());
			forecast.setErrorMessage("request_id can't be empty");
			return forecast;
		}

		if (whsId == null) {
			LOG.error("whs_id can't be empty" + forecastParameters.toString());
			forecast.setErrorMessage("whs_id can't be empty");
			return forecast;
		}
		if (artId == null) {
			LOG.error("art_id can't be empty" + forecastParameters.toString());
			forecast.setErrorMessage("art_id can't be empty");
			return forecast;
		}
		if (trainingStart == null || trainingStart.trim().equals("")) {
			LOG.error("training_start can't be empty" + forecastParameters.toString());
			forecast.setErrorMessage("training_start can't be empty");
			return forecast;
		}
		if (trainingEnd == null || trainingEnd.trim().equals("")) {
			LOG.error("training_end can't be empty" + forecastParameters.toString());
			forecast.setErrorMessage("training_end can't be empty");
			return forecast;
		}
		
		LocalDate startDate = LocalDate.parse(trainingStart);
		LocalDate endDate = LocalDate.parse(trainingEnd);
		
		if(!startDate.isBefore(endDate)){
			LOG.error("Start date of forecasting is earlier than First date of analysis: " + forecastParameters.toString());
			forecast.setErrorMessage("Start date of forecasting is earlier than First date of analysis");
			return forecast;
		}
		
		try {
			SqlRowSet salesRowSet = dataRepository.getSalesWithPrices(forecastParameters);
			forecast = dataRepository.getForecast(forecastParameters, salesRowSet);
			return forecast;
		} catch (Exception e) {
			forecast.setErrorMessage(e.toString());
			return forecast;
		}
	}

	@Override
	public String createForecastResultFileSingleDatabaseMode(ResponseForecast responseForecast) throws DataServiceException  {
		String result = dataRepository.createForecastResultFile(responseForecast);
		return result;
	}

	@Override
	public String createForecastResultFileMultipleDatabaseMode(List<ResponseForecast> responseForecastList) throws DataServiceException {
		String result = dataRepository.createForecastMultipleResultFile(responseForecastList);
		return result;
	}
	
	@Override
	public void sturpUpFileStorages() throws DataServiceException {
		try {
			if(Files.notExists(this.inputFolderLocation, LinkOption.NOFOLLOW_LINKS)){
				Files.createDirectory(this.inputFolderLocation);
			}
			if(Files.notExists(this.outputFolderLocation, LinkOption.NOFOLLOW_LINKS)){
				Files.createDirectory(this.outputFolderLocation);
			}
		} catch (IOException e) {
			LOG.error("Could not initialize storage:" + e);
			throw new DataServiceException("Could not initialize storage:" + e);
		}
	}
	
	@Override
	public String putFileInInputStorage(MultipartFile file) throws DataServiceException {
		try {
            if (file.isEmpty()) {
            	LOG.error("File is empty: " + file.getOriginalFilename());
                throw new DataServiceException("File is empty: " + file.getOriginalFilename());
            }
			String fileName = RandomStringUtils.randomAlphanumeric(32) +
					file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
            Files.copy(file.getInputStream(), this.inputFolderLocation.resolve(fileName));
			return fileName;
        } catch (IOException e) {
        	LOG.error("Can't save file:  " + file.getOriginalFilename() + " " + e.toString());
			throw new DataServiceException("Can't save file:  " + file.getOriginalFilename() + " " + e.toString());
        }
	}

	@Override
	public Path getFilePathFromInputStorage(String filename) {
		 return this.inputFolderLocation.resolve(filename);
	}
	
	@Override
	public Path getFilePathFromOutputStorage(String filename) {
		return this.outputFolderLocation.resolve(filename);
	}

	@Override
	public Stream<Path> getFilePathAllFromInputStorage() throws DataServiceException {
		try {
			return Files
						.walk(this.inputFolderLocation, 1)
						.filter(path -> !path.equals(this.inputFolderLocation))
						.map(path -> this.inputFolderLocation.relativize(path));
		} catch (IOException e) {
			LOG.error("Can't read input folder files:" + e.toString());
			throw new DataServiceException("Can't read input folder files:" + e.toString());
		}
	}
	
	@Override
	public Stream<Path> getFilePathAllFromOutputStorage() throws DataServiceException {
		try {
			return Files
						.walk(this.outputFolderLocation, 1)
						.filter(path -> !path.equals(this.outputFolderLocation))
						.map(path -> this.outputFolderLocation.relativize(path));
		} catch (IOException e) {
			LOG.error("Can't read output folder files:" + e.toString());
			throw new DataServiceException("Can't read output folder files:" + e.toString());
		}
	}

	@Override
	public Resource getFileFromInputStorageAsResourse(String filename) throws DataServiceException {
        try {
            Path file = getFilePathFromInputStorage(filename);
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
            	LOG.error("Can't read file in input storage: " + filename);
                throw new DataServiceException("Can't read file in input storage: " + filename);
            }
        } catch (MalformedURLException e) {
        	LOG.error("Can't read file in input storage: " + filename);
            throw new DataServiceException("Can't read file in input storage: " + filename + " " + e.toString());
        }
	}
	
	@Override
	public Resource getFileFromOutputStorageAsResourse(String filename) throws DataServiceException {
		  try {
	            Path file = getFilePathFromOutputStorage(filename);
	            Resource resource = new UrlResource(file.toUri());
	            if(resource.exists() || resource.isReadable()) {
	                return resource;
	            }
	            else {
	            	LOG.error("Can't read file in output storage: " + filename);
	                throw new DataServiceException("Can't read file in output storage: " + filename);
	            }
	        } catch (MalformedURLException e) {
	        	LOG.error("Can't read file in output storage: " + filename);
	            throw new DataServiceException("Can't read file in output storage: " + filename + " " + e.toString());
	        }
	}
	
	@Override
	public void deleteAllFilesInInputFilesStorage() {
		FileSystemUtils.deleteRecursively(this.inputFolderLocation.toFile());
	}

	@Override
	public List<ResponseForecast> getForecastExcelMode(Integer requestId) throws DataServiceException {
		List<ResponseForecast> responseList = new ArrayList<>();
		List<RequestForecastParameterSingle> requestForecastParameterSingleList = dataRepository.getRequestForecastParameterSingleList(requestId);
		ExecutorService executorService = Executors.newFixedThreadPool(countThreads);
		List<Future<ResponseForecast>> futureResponsetList = new ArrayList<Future<ResponseForecast>>();

		for (int i = 0; i < requestForecastParameterSingleList.size(); i++) {
			RequestForecastParameterSingle forecastParameter = requestForecastParameterSingleList.get(i);
			Future<ResponseForecast> futureResponse = executorService.submit(() -> {
				return this.getForecastSingleDatabaseMode(forecastParameter);
			});
			futureResponsetList.add(futureResponse);
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
		
		//Getting results from tasks
		for (Future<ResponseForecast> futureResponse : futureResponsetList) {
			ResponseForecast response;
			try {
				response = futureResponse.get();
				responseList.add(response);
				//Mark that request as completed
				requestForecastParameterSingleList.removeIf(e->e.getArtId().equals(response.getArtId()) && e.getWhsId().equals(response.getWhsId()));
				
			} catch (Exception e) {
				LOG.error("Getting forecast result exception: " + e.toString());
			}
		}
		
		//Making complete response
		for(RequestForecastParameterSingle requestParameter : requestForecastParameterSingleList){
			ResponseForecast response = new ResponseForecast(requestParameter.getWhsId(), requestParameter.getArtId());
			response.setErrorMessage("Couldn't make forecast");
			responseList.add(response);
		}
		return responseList;
	}

	@Override
	public String createForecastResultFileExcelMode(List<ResponseForecast> responseForecastList) throws DataServiceException {
		String filePath = this.createForecastResultFileMultipleDatabaseMode(responseForecastList);
		return filePath;
	}

	@Override
	public void deleteFileFromInputStorage(String filename) {
		Path path = this.inputFolderLocation.resolve(filename);
		if(path!=null){
			try {
				Files.delete(path);
			} catch (IOException e) {
				LOG.error("Can't delete file in input storage:" + filename + " ." + e.toString());
			}
		}
	}

	@Override
	public void deleteFileFromOutputStorage(String filename) {
		Path path = this.outputFolderLocation.resolve(filename);
		if(path!=null){
			try {
				Files.delete(path);
			} catch (IOException e) {
				LOG.error("Can't delete file in output storage:" + filename + " ." + e.toString());
			}
		}
	}
	
	@Override
	public List<String> getAttachmentPathListByResponseTimeBeforeMoment(Date dateBound) {
		return dataRepository.getAttachmentPathListByResponseTimeBeforeMoment(dateBound);
	}
	
	@Override
	public List<String> getDocumentPathListByResponseTimeBeforeMoment(Date dateBound) {
		return dataRepository.getDocumentPathListByResponseTimeBeforeMoment(dateBound);
	}
	
	@Override
	public Integer deleteRequestByResponseTimeBeforeMoment(Date dateBound) {
		return dataRepository.deleteRequestByResponseTimeBeforeMoment(dateBound);
	}

	@Override
	public String createElasticityResultFileExcelMode(List<ResponseElasticity> elasticityResponseList) throws DataServiceException {
		String filePath = this.createElasticityResultFileMultipleDatabaseMode(elasticityResponseList);
		return filePath;
	}

	public String createElasticityResultFileMultipleDatabaseMode(List<ResponseElasticity> elasticityResponseList) throws DataServiceException {
		String result = dataRepository.createElasticityMultipleResultFile(elasticityResponseList);
		return result;
	}

	@Override
	public String createForecastAndElasticityResultFileExcelMode(List<ResponseForecastAndElasticity> forecastAndElasticityResponseList) throws DataServiceException {
		String filePath = this.getForecastWithElasticityFileMultipleDatabaseMode(forecastAndElasticityResponseList);
		return filePath;
	}

	private String getForecastWithElasticityFileMultipleDatabaseMode(List<ResponseForecastAndElasticity> forecastAndElasticityResponseList) throws DataServiceException {
		String result = dataRepository.createForecastWithElasticityMultipleResultFile(forecastAndElasticityResponseList);
		return result;
	}

	@Override
	public String createForecastAndElasticityResultFileMultipleDatabaseMode(List<ResponseForecastAndElasticity> forecastAndElastisityResponse) throws DataServiceException {
		String result = dataRepository.createForecastWithElasticityMultipleResultFile(forecastAndElastisityResponse);
		return result;
	}
}
