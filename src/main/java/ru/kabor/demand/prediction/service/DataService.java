package ru.kabor.demand.prediction.service;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ru.kabor.demand.prediction.entity.RequestForecastParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponceForecast;

@Service
public interface DataService {
	/** Forecast for inner database*/
	ResponceForecast getForecastSingle(RequestForecastParameterSingle forecastParameters);
	List<ResponceForecast> getForecastMultiple(RequestForecastParameterMultiple forecastParameters);
	String getForecastFileSingle(ResponceForecast responceForecast) throws Exception;
	String getForecastFileMultiple(List<ResponceForecast>  responceForecastList) throws Exception;
	
	/** Forecast for sending excelFiles */
	
	/** If folder for keeping excel doesn't exists it creates that method
	 * @throws DataServiceException*/
    void sturpUpFileStorage() throws DataServiceException;
    /** Put one file into storage folder
     * @throws DataServiceException*/
    void putFile(MultipartFile file) throws DataServiceException;
    /** Get path for file in storage folder*/
    Path getStorageFilePath(String filename);
    /** Get path for all files in storage folder
     * @throws DataServiceException*/
    Stream<Path> getStorageFilePathAll() throws DataServiceException;
    /** Get file from storage as resource
     * @throws DataServiceException*/
    Resource getStorageFileAsResourse(String filename) throws DataServiceException;
    /** Deletes all files in storage*/
    void deleteAllFiles();
    /** Get available document for processing*/
    String getAvailableDocument();
}
