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

import ru.kabor.demand.prediction.entity.RequestForecastParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponceForecast;
import ru.kabor.demand.prediction.repository.DataRepository;

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
	
	
	@PostConstruct
	private void postConstruct() throws DataServiceException{
		this.sturpUpFileStorage();
	}
	
	@Override
	public List<ResponceForecast> getForecastMultipleDatabaseMode(RequestForecastParameterMultiple forecastParameters) throws DataServiceException {
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
		
		List<ResponceForecast> forecastList = null;
		try {
			SqlRowSet salesRowSet = dataRepository.getSalesMultiple(forecastParameters);
			forecastList = dataRepository.getForecastMultiple(forecastParameters, salesRowSet);
		} catch (Exception e) {
			throw new DataServiceException(e.toString());
		}
		return forecastList;
	}

	@Override
	public ResponceForecast getForecastSingleDatabaseMode(RequestForecastParameterSingle forecastParameters){
		
		ResponceForecast forecast;
		
		Integer requestId = forecastParameters.getRequestId();
		Integer whsId = forecastParameters.getWhsId();
		Integer artId = forecastParameters.getArtId();
		String trainingStart = forecastParameters.getTrainingStart();
		String trainingEnd = forecastParameters.getTrainingEnd();
		
		forecast = new ResponceForecast(whsId, artId);
		
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
			SqlRowSet salesRowSet = dataRepository.getSales(forecastParameters);
			forecast = dataRepository.getForecast(forecastParameters, salesRowSet);
			return forecast;
		} catch (Exception e) {
			forecast.setErrorMessage(e.toString());
			return forecast;
		}
	}

	@Override
	public String getForecastFileSingleDatabaseMode(ResponceForecast responceForecast) throws DataServiceException  {
		String result = dataRepository.getForecastFile(responceForecast);
		return result;
	}

	@Override
	public String getForecastFileMultipleDatabaseMode(List<ResponceForecast> responceForecastList) throws DataServiceException {
		String result = dataRepository.getForecastFileMultiple(responceForecastList);
		return result;
	}
	
	/** If folder for keeping excel doesn't exists it creates that method
	 * @throws DataServiceException*/
	@Override
	public void sturpUpFileStorage() throws DataServiceException {
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
	
    /** Put one file into storage folder
     * @throws DataServiceException*/
	@Override
	public String putFile(MultipartFile file) throws DataServiceException {
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

	/** Get path for file in input storage folder*/
	@Override
	public Path getStorageInputFilePath(String filename) {
		 return this.inputFolderLocation.resolve(filename);
	}
	
	/** Get path for file in output storage folder*/
	@Override
	public Path getStorageOutputFilePath(String filename) {
		return this.outputFolderLocation.resolve(filename);
	}

	/** Get path for all files in input storage folder
     * @throws DataServiceException*/
	@Override
	public Stream<Path> getStorageInputFilePathAll() throws DataServiceException {
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
	
	/** Get path for all files in output storage folder
     * @throws DataServiceException*/
	@Override
	public Stream<Path> getStorageOutputFilePathAll() throws DataServiceException {
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

	/** Get file from input storage as resource
     * @throws DataServiceException*/
	@Override
	public Resource getStorageInputFileAsResourse(String filename) throws DataServiceException {
        try {
            Path file = getStorageInputFilePath(filename);
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
	
	/** Get file from output storage as resource
     * @throws DataServiceException*/
	@Override
	public Resource getStorageOutputFileAsResourse(String filename) throws DataServiceException {
		  try {
	            Path file = getStorageOutputFilePath(filename);
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
	
	/** Deletes all files in storage*/
	@Override
	public void deleteAllFiles() {
		FileSystemUtils.deleteRecursively(this.inputFolderLocation.toFile());
	}

	@Override
	public List<ResponceForecast> getForecastExcelMode(Integer requestId) throws DataServiceException {
		List<ResponceForecast> responceList = new ArrayList<>();
		List<RequestForecastParameterSingle> requestForecastParameterSingleList = dataRepository.getRequestForecastParameterSingleList(requestId);
		
		//Sending tasks to execute
		ExecutorService executorService = Executors.newFixedThreadPool(countThreads);
		List<Future<ResponceForecast>> futureResponsetList = new ArrayList<Future<ResponceForecast>>();

		for (int i = 0; i < requestForecastParameterSingleList.size(); i++) {
			RequestForecastParameterSingle forecastParameter = requestForecastParameterSingleList.get(i);
			Future<ResponceForecast> futureResponse = executorService.submit(() -> {
				return this.getForecastSingleDatabaseMode(forecastParameter);
			});
			futureResponsetList.add(futureResponse);
		}

		executorService.shutdown();
		try {
			executorService.awaitTermination(20000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new DataServiceException("R is not answerring too long");
		} finally{
			if(!executorService.isShutdown()){
				executorService.shutdownNow();
			}
		}
		
		//Getting results from tasks
		for (Future<ResponceForecast> futureResponse : futureResponsetList) {
			ResponceForecast responce;
			try {
				responce = futureResponse.get();
				responceList.add(responce);
				//Mark that request as completed
				requestForecastParameterSingleList.removeIf(e->e.getArtId().equals(responce.getArtId()) && e.getWhsId().equals(responce.getWhsId()));
				
			} catch (Exception e) {
				LOG.error("Getting forecast result exception: " + e.toString());
			}
		}
		
		//Making complete response
		for(RequestForecastParameterSingle requestParameter : requestForecastParameterSingleList){
			ResponceForecast responce = new ResponceForecast(requestParameter.getWhsId(), requestParameter.getArtId());
			responce.setErrorMessage("Couldn't make forecast");
			responceList.add(responce);
		}
		return responceList;
	}

	@Override
	public String getForecastFileExcelMode(List<ResponceForecast> responceForecastList) throws DataServiceException {
		String filePath = this.getForecastFileMultipleDatabaseMode(responceForecastList);
		return filePath;
	}

	/** Delete file in input storage*/
	@Override
	public void deleteFileStorageInput(String filename) {
		Path path = this.inputFolderLocation.resolve(filename);
		if(path!=null){
			try {
				Files.delete(path);
			} catch (IOException e) {
				LOG.error("Can't delete file in input storage:" + filename + " ." + e.toString());
			}
		}
	}

	/** Delete file in output storage*/
	@Override
	public void deleteFileStorageOutput(String filename) {
		Path path = this.outputFolderLocation.resolve(filename);
		if(path!=null){
			try {
				Files.delete(path);
			} catch (IOException e) {
				LOG.error("Can't delete file in output storage:" + filename + " ." + e.toString());
			}
		}
	}
	
	/** Get v_request.attachment_path where v_request.response_date_time before moment of time*/
	@Override
	public List<String> getAttachmentPathListByResponseTimeBeforeMoment(Date dateBound) {
		return dataRepository.getAttachmentPathListByResponseTimeBeforeMoment(dateBound);
	}
	
	/** Get v_request.document_path where v_request.response_date_time before moment of time*/
	@Override
	public List<String> getDocumentPathListByResponseTimeBeforeMoment(Date dateBound) {
		return dataRepository.getDocumentPathListByResponseTimeBeforeMoment(dateBound);
	}
	
	/** Delete all request where v_request.response_date_time before moment of time*/
	@Override
	public Integer deleteRequestByResponseTimeBeforeMoment(Date dateBound) {
		return dataRepository.deleteRequestByResponseTimeBeforeMoment(dateBound);
	}
}
