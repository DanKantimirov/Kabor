package ru.kabor.demand.prediction.service;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ru.kabor.demand.prediction.entity.RequestElasticityParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestElasticityParameterSingle;
import ru.kabor.demand.prediction.entity.RequestForecastAndElasticityParameterSingle;
import ru.kabor.demand.prediction.entity.RequestForecastParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponceElasticity;
import ru.kabor.demand.prediction.entity.ResponceForecast;
import ru.kabor.demand.prediction.entity.ResponceForecastAndElasticity;

/** It contains general methods */
@Service
public interface DataService {
	
	// ===========================File storages==========================================
	/** creating folder for input and output excel files if it doesn't exist */
    void sturpUpFileStorages() throws DataServiceException;
    /** putting file into input files foler */
    String putFileInInputStorage(MultipartFile file) throws DataServiceException;
    /** getting path for file in input files folder */
    Path getFilePathFromInputStorage(String filename);
    /** getting path for file in output files folder */
    Path getFilePathFromOutputStorage(String filename);
    /** deleting file from input files storage */
    void deleteFileFromInputStorage(String filename);
    /** deleting file from output file storage */
    void deleteFileFromOutputStorage(String filename);
    /** getting stream of path for all files from input files storage */
    Stream<Path> getFilePathAllFromInputStorage() throws DataServiceException;
    /** getting stream of path for all files from output files storage */
    Stream<Path> getFilePathAllFromOutputStorage() throws DataServiceException;
    /** getting file from input files storage as resource */
    Resource getFileFromInputStorageAsResourse(String filename) throws DataServiceException;
    /** getting file from output files storage as resource */
    Resource getFileFromOutputStorageAsResourse(String filename) throws DataServiceException;
    /** deleting all files in input files storage */
    void deleteAllFilesInInputFilesStorage();
	
	// ===========================Forecast for Database mode=================================
    /** making forecast (one whs and one sku) */
	ResponceForecast getForecastSingleDatabaseMode(RequestForecastParameterSingle forecastParameters);
	/** making forecast (many whs and many sku) */
	List<ResponceForecast> getForecastMultipleDatabaseMode(RequestForecastParameterMultiple forecastParameters) throws DataServiceException;
	/** creating report with result of forecast (one whs and one sku) */
	String createForecastResultFileSingleDatabaseMode(ResponceForecast responceForecast) throws DataServiceException;
	/** creating report with result of forecast (many whs and many sku) */
	String createForecastResultFileMultipleDatabaseMode(List<ResponceForecast> responceForecastList) throws DataServiceException;
	
	// ===========================Forecast for Excel mode=====================================
	/** making forecast (many whs and many sku) */
	List<ResponceForecast> getForecastExcelMode(Integer requestId) throws DataServiceException;
	/** creating report with result of forecast (many whs and many sku) */
	String createForecastResultFileExcelMode(List<ResponceForecast> responceForecastList) throws DataServiceException;

	// ===========================Elasticity for Database mode=================================
	/** calculating elasticity (one whs and one sku) */
	ResponceElasticity getElasticitySingleDatabaseMode(RequestElasticityParameterSingle elasticityParameter);
	/** calculating elasticity (many whs and many sku) */
	List<ResponceElasticity> getElasticityMultipleDatabaseMode(RequestElasticityParameterMultiple elasticityParameterMultiple) throws DataServiceException;
	/** creating report with result of elasticity (many whs and many sku) */
	String createElasticityResultFileMultipleDatabaseMode(List<ResponceElasticity> elasticityResponseList) throws DataServiceException;
	
	// ===========================Elasticity for Excel mode=====================================
	/** calculating elasticity (many whs and many sku) */
	List<ResponceElasticity> getElasticitytExcelMode(Integer requestId) throws DataServiceException;
	/** creating report with result of elasticity (many whs and many sku) */
	String createElasticityResultFileExcelMode(List<ResponceElasticity> elasticityResponseList) throws DataServiceException;
	
	// ===========================Forecast and Elasticity for Database mode=====================================
	/** making forecast and calculating elasticity (one whs and one sku) */
	ResponceForecastAndElasticity getForecastAndElasticitySingleDatabaseMode(RequestForecastAndElasticityParameterSingle forecastAndElasticityParameter);
	
	// ===========================Forecast and Elasticity for Excel mode=====================================
	/** making forecast and calculating elasticity (many whs and many sku) */
	List<ResponceForecastAndElasticity> getForecastAndElasticitytExcelMode(Integer requestId) throws DataServiceException;
	/** creating report with result of forecast and elasticity (many whs and many sku) */
	String createForecastAndElasticityResultFileExcelMode(List<ResponceForecastAndElasticity> forecastAndElasticityResponseList) throws DataServiceException;

	// ===========================Cleaning from old requests======================================
    /** Get v_request.attachment_path where v_request.response_date_time before moment of time */
	List<String> getAttachmentPathListByResponseTimeBeforeMoment(Date dateBound);
	/** Get v_request.document_path where v_request.response_date_time before moment of time */
	List<String> getDocumentPathListByResponseTimeBeforeMoment(Date dateBound);
	/** Delete all request where v_request.response_date_time before moment of time */
	Integer deleteRequestByResponseTimeBeforeMoment(Date dateBound);
}
