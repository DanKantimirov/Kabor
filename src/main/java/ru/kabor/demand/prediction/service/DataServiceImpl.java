package ru.kabor.demand.prediction.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
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
	
	
	@PostConstruct
	private void postConstruct() throws DataServiceException{
		this.sturpUpFileStorage();
		//this.deleteAllFiles();
	}
	
	@Override
	public List<ResponceForecast> getForecastMultiple(RequestForecastParameterMultiple forecastParameters) {
		String whsIdBulk = forecastParameters.getWhsIdBulk();
		String artIdBulk = forecastParameters.getArtBulk();
		String trainingStart = forecastParameters.getTrainingStart();
		String trainingEnd = forecastParameters.getTrainingEnd();
		
		if (whsIdBulk == null || whsIdBulk.trim().equals("")) {
			LOG.error("whs_id can't be empty" + forecastParameters.toString());
			throw new IllegalArgumentException("whs_id не может быть пустым");
		}
		if (artIdBulk == null || artIdBulk.trim().equals("")) {
			LOG.error("art_id can't be empty" + forecastParameters.toString());
			throw new IllegalArgumentException("art_id не может быть пустым");
		}
		whsIdBulk = whsIdBulk.trim();
		artIdBulk = artIdBulk.trim();
		
		Pattern patternForCheck = Pattern.compile("^[0-9;]+");  
        Matcher matcherForCheck = patternForCheck.matcher(whsIdBulk);  
		
		if(!matcherForCheck.matches()){
			throw new IllegalArgumentException("в whs_id доступны символы только (0-9 или ;)");
		}
		
        matcherForCheck = patternForCheck.matcher(artIdBulk);  
		
		if(!matcherForCheck.matches()){
			throw new IllegalArgumentException("в art_id доступны символы только (0-9 или ;)");
		}
		
		if (trainingStart == null || trainingStart.trim().equals("")) {
			LOG.error("training_start can't be empty" + forecastParameters.toString());
			throw new IllegalArgumentException("training_start не может быть пустым");
		}
		if (trainingEnd == null || trainingEnd.trim().equals("")) {
			LOG.error("training_end can't be empty" + forecastParameters.toString());
			throw new IllegalArgumentException("training_end не может быть пустым");
		}
		
		LocalDate startDate = LocalDate.parse(trainingStart);
		LocalDate endDate = LocalDate.parse(trainingEnd);
		
		if(!startDate.isBefore(endDate)){
			LOG.error("training_end is before training_start" + forecastParameters.toString());
			throw new IllegalArgumentException("Дата начала прогноза раньше даты начала анализа");
		}
		
		List<ResponceForecast> forecastList = null;
		try {
			SqlRowSet salesRowSet = dataRepository.getSalesMultiple(forecastParameters);
			forecastList = dataRepository.getForecastMultiple(forecastParameters, salesRowSet);
		} catch (Exception e) {
			throw new UnsupportedOperationException(e.toString());
		}
		return forecastList;
	}

	@Override
	public ResponceForecast getForecastSingle(RequestForecastParameterSingle forecastParameters){

		Integer whsId = forecastParameters.getWhsId();
		Integer artId = forecastParameters.getArtId();
		String trainingStart = forecastParameters.getTrainingStart();
		String trainingEnd = forecastParameters.getTrainingEnd();

		if (whsId == null) {
			LOG.error("whs_id can't be empty" + forecastParameters.toString());
			throw new IllegalArgumentException("whs_id не может быть пустым");
		}
		if (artId == null) {
			LOG.error("art_id can't be empty" + forecastParameters.toString());
			throw new IllegalArgumentException("art_id не может быть пустым");
		}
		if (trainingStart == null || trainingStart.trim().equals("")) {
			LOG.error("training_start can't be empty" + forecastParameters.toString());
			throw new IllegalArgumentException("training_start не может быть пустым");
		}
		if (trainingEnd == null || trainingEnd.trim().equals("")) {
			LOG.error("training_end can't be empty" + forecastParameters.toString());
			throw new IllegalArgumentException("training_end не может быть пустым");
		}
		
		LocalDate startDate = LocalDate.parse(trainingStart);
		LocalDate endDate = LocalDate.parse(trainingEnd);
		
		if(!startDate.isBefore(endDate)){
			LOG.error("training_end is before training_start" + forecastParameters.toString());
			throw new IllegalArgumentException("Дата начала прогноза раньше даты начала анализа");
		}
		
		ResponceForecast forecast;
		try {
			SqlRowSet salesRowSet = dataRepository.getSales(forecastParameters);
			forecast = dataRepository.getForecast(forecastParameters,salesRowSet);
		} catch (Exception e) {
			throw new UnsupportedOperationException(e.toString());
		}
		return forecast;
	}

	@Override
	public String getForecastFileSingle(ResponceForecast responceForecast) throws Exception {
		String result = dataRepository.getForecastFile(responceForecast);
		return result;
	}

	@Override
	public String getForecastFileMultiple(List<ResponceForecast> responceForecastList) throws Exception {
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
		//TODO: add insert information to database
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
}
