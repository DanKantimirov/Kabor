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
import ru.kabor.demand.prediction.entity.RequestForecastAndElasticityParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastAndElasticityParameterSingle;
import ru.kabor.demand.prediction.entity.RequestForecastParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponseElasticity;
import ru.kabor.demand.prediction.entity.ResponseForecast;
import ru.kabor.demand.prediction.entity.ResponseForecastAndElasticity;

/** It contains general methods */
@Service
public interface DataService {
	
	// ===========================File storages==========================================
   
	/** creating folder for input and output excel files if it doesn't exist
     * @throws DataServiceException
     */
    void sturpUpFileStorages() throws DataServiceException;
   
    /** putting file into input files foler 
     * @param file file
     * @return filename
     * @throws DataServiceException
     */
    String putFileInInputStorage(MultipartFile file) throws DataServiceException;
   
    /** getting path for file in input files folder
     * @param filename filename
     * @return path
     */
    Path getFilePathFromInputStorage(String filename);
   
    /** getting path for file in output files folder 
     * @param filename filename
     * @return path
     */
    Path getFilePathFromOutputStorage(String filename);
   
    /** deleting file from input files storage
     * @param filename filename
     */
    void deleteFileFromInputStorage(String filename);
   
    /** deleting file from output file storage
     * @param filename filename
     */
    void deleteFileFromOutputStorage(String filename);
   
    /** getting stream of path for all files from input files storage 
     * @return stream of path 
     * @throws DataServiceException
     */
    Stream<Path> getFilePathAllFromInputStorage() throws DataServiceException;
   
    /** getting stream of path for all files from output files storage
     * @return stream of path
     * @throws DataServiceException
     */
    Stream<Path> getFilePathAllFromOutputStorage() throws DataServiceException;
    
    /** getting file from input files storage as resource
     * @param filename filename
     * @return
     * @throws DataServiceException
     */
    Resource getFileFromInputStorageAsResourse(String filename) throws DataServiceException;
   
    /** getting file from output files storage as resource
     * @param filename filename
     * @return resource
     * @throws DataServiceException
     */
    Resource getFileFromOutputStorageAsResourse(String filename) throws DataServiceException;
   
    /** deleting all files in input files storage */
    void deleteAllFilesInInputFilesStorage();
	
	// ===========================Forecast for Database mode=================================
	
    /** making forecast (one shop and one product)
	 * @param forecastParameters parameters of forecast
	 * @return response of forecast
	 */
	ResponseForecast getForecastSingleDatabaseMode(RequestForecastParameterSingle forecastParameters);
	
	/** making forecast (many shops and many products) 
	 * @param forecastParameters parameters of forecast
	 * @return list of forecasts' responses
	 * @throws DataServiceException
	 */
	List<ResponseForecast> getForecastMultipleDatabaseMode(RequestForecastParameterMultiple forecastParameters) throws DataServiceException;
	
	/** creating report with result of forecast (one shop and one product)
	 * @param responseForecast response of forecast
	 * @return filename
	 * @throws DataServiceException
	 */
	String createForecastResultFileSingleDatabaseMode(ResponseForecast responseForecast) throws DataServiceException;
	
	/** creating report with result of forecast (many shops and many products)
	 * @param responseForecastList response of forecast
	 * @return filename
	 * @throws DataServiceException
	 */
	String createForecastResultFileMultipleDatabaseMode(List<ResponseForecast> responseForecastList) throws DataServiceException;
	
	// ===========================Forecast for Excel mode=====================================
	
	/** making forecast (many shop and many products)
	 * @param requestId id of request
	 * @return list of forecasts' responses
	 * @throws DataServiceException
	 */
	List<ResponseForecast> getForecastExcelMode(Integer requestId) throws DataServiceException;
	
	/** creating report with result of forecast (many shops and many products)
	 * @param responseForecastList list of forecasts' responses
	 * @return filename
	 * @throws DataServiceException
	 */
	String createForecastResultFileExcelMode(List<ResponseForecast> responseForecastList) throws DataServiceException;

	// ===========================Elasticity for Database mode=================================
	
	/** calculating elasticity (one shop and one products)
	 * @param elasticityParameter elasticity parameters
	 * @return response of elasticity
	 */
	ResponseElasticity getElasticitySingleDatabaseMode(RequestElasticityParameterSingle elasticityParameter);
	
	/**  calculating elasticity (many shops and many products)
	 * @param elasticityParameterMultiple  elasticity parameters
	 * @return list of elasticities' responses
	 * @throws DataServiceException
	 */
	List<ResponseElasticity> getElasticityMultipleDatabaseMode(RequestElasticityParameterMultiple elasticityParameterMultiple) throws DataServiceException;
	
	/** creating report with result of elasticity (many shops and many products)
	 * @param elasticityResponseList list of elasticities' responses
	 * @return filename
	 * @throws DataServiceException
	 */
	String createElasticityResultFileMultipleDatabaseMode(List<ResponseElasticity> elasticityResponseList) throws DataServiceException;
	
	// ===========================Elasticity for Excel mode=====================================
	
	/** calculating elasticity (many shops and many products)
	 * @param requestId id of request
	 * @return list of elasticities' responses
	 * @throws DataServiceException
	 */
	List<ResponseElasticity> getElasticitytExcelMode(Integer requestId) throws DataServiceException;
	
	/** creating report with result of elasticity (many shops and many products)
	 * @param elasticityResponseList list of elasticities' responses
	 * @return filename
	 * @throws DataServiceException
	 */
	String createElasticityResultFileExcelMode(List<ResponseElasticity> elasticityResponseList) throws DataServiceException;
	
	// ===========================Forecast and Elasticity for Database mode=====================================
	/** making forecast and calculating elasticity (one shop and one product)
	 * @param forecastAndElasticityParameter forecasts and elasticities parameters
	 * @return result of making forecast and calculating elastisity
	 */
	ResponseForecastAndElasticity getForecastAndElasticitySingleDatabaseMode(RequestForecastAndElasticityParameterSingle forecastAndElasticityParameter);
	
	/** making forecast and calculating elasticity (many shops and one products)
	 * @param forecastAndElasticityParameterMultiple forecasts and elasticities parameters
	 * @return result of making forecast and calculating elastisity
	 * @throws DataServiceException 
	 */
	List<ResponseForecastAndElasticity> getForecastAndElasticityMultipleDatabaseMode(RequestForecastAndElasticityParameterMultiple forecastAndElasticityParameterMultiple) throws DataServiceException;
	
	/** creating report with result of forecast and elasticity (many shops and many products)
	 * @param forecastAndElastisityResponse list of forecasts' and elasticities' responses
	 * @return filename
	 * @throws DataServiceException 
	 */
	String createForecastAndElasticityResultFileMultipleDatabaseMode(List<ResponseForecastAndElasticity> forecastAndElastisityResponse) throws DataServiceException;
	
	// ===========================Forecast and Elasticity for Excel mode=====================================
	
	/** making forecast and calculating elasticity (many shops and many products)
	 * @param requestId id of request
	 * @return result of making forecast and calculating elastisity (list)
	 * @throws DataServiceException
	 */
	List<ResponseForecastAndElasticity> getForecastAndElasticitytExcelMode(Integer requestId) throws DataServiceException;
	
	/** creating report with result of forecast and elasticity (many shops and many products)
	 * @param forecastAndElasticityResponseList result of making forecast and calculating elastisity (list)
	 * @return filename
	 * @throws DataServiceException
	 */
	String createForecastAndElasticityResultFileExcelMode(List<ResponseForecastAndElasticity> forecastAndElasticityResponseList) throws DataServiceException;

	// ===========================Cleaning from old requests======================================
    
	/** Get v_request.attachment_path where v_request.response_date_time before moment of time 
	 * @param dateBound moment of time
	 * @return  v_request.attachment_path(list)
	 */
	List<String> getAttachmentPathListByResponseTimeBeforeMoment(Date dateBound);
	
	/** Get v_request.document_path where v_request.response_date_time before moment of time 
	 * @param dateBound moment of time
	 * @return v_request.document_path(list)
	 */
	List<String> getDocumentPathListByResponseTimeBeforeMoment(Date dateBound);
	
	/** Delete all request where v_request.response_date_time before moment of time
	 * @param dateBound moment of time
	 * @return count deleted requests
	 */
	Integer deleteRequestByResponseTimeBeforeMoment(Date dateBound);
}
