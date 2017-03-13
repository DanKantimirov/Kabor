package ru.kabor.demand.prediction.service;

import java.nio.file.Path;
import java.util.Date;
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
	ResponceForecast getForecastSingleDatabaseMode(RequestForecastParameterSingle forecastParameters) throws DataServiceException;
	List<ResponceForecast> getForecastMultipleDatabaseMode(RequestForecastParameterMultiple forecastParameters) throws DataServiceException;
	String getForecastFileSingleDatabaseMode(ResponceForecast responceForecast) throws DataServiceException;
	String getForecastFileMultipleDatabaseMode(List<ResponceForecast> responceForecastList) throws DataServiceException;
	
	/** Forecast for sending excelFiles */
	List<ResponceForecast> getForecastExcelMode(Integer requestId) throws DataServiceException; 
	String getForecastFileExcelMode(List<ResponceForecast> responceForecastList) throws DataServiceException;
	
	/** If folder for keeping excel doesn't exists it creates that method
	 * @throws DataServiceException*/
    void sturpUpFileStorage() throws DataServiceException;

    /**
     * Put one file into storage folder
     * @param file
     * @return
     * @throws DataServiceException
     */
    String putFile(MultipartFile file) throws DataServiceException;
    /** Get path for file in input storage folder*/
    Path getStorageInputFilePath(String filename);
    /** Get path for file in output storage folder*/
    Path getStorageOutputFilePath(String filename);
    /** Get path for all files in input storage folder
     * @throws DataServiceException*/
    Stream<Path> getStorageInputFilePathAll() throws DataServiceException;
    /** Get path for all files in output storage folder
     * @throws DataServiceException*/
    Stream<Path> getStorageOutputFilePathAll() throws DataServiceException;
    /** Get file from input storage as resource
     * @throws DataServiceException*/
    Resource getStorageInputFileAsResourse(String filename) throws DataServiceException;
    /** Get file from output storage as resource
     * @throws DataServiceException*/
    Resource getStorageOutputFileAsResourse(String filename) throws DataServiceException;
    /** Deletes all files in storage*/
    void deleteAllFiles();
    /** Delete file in input storage*/
    void deleteFileStorageInput(String filename);
    /** Delete file in output storage*/
    void deleteFileStorageOutput(String filename);
    /** Get v_request.attachment_path where v_request.response_date_time before moment of time*/
	List<String> getAttachmentPathListByResponseTimeBeforeMoment(Date dateBound);
	/** Get v_request.document_path where v_request.response_date_time before moment of time*/
	List<String> getDocumentPathListByResponseTimeBeforeMoment(Date dateBound);
	/** Delete all request where v_request.response_date_time before moment of time*/
	Integer deleteRequestByResponseTimeBeforeMoment(Date dateBound);
}
