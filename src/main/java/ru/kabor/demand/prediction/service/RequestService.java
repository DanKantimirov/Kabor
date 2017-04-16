package ru.kabor.demand.prediction.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.web.multipart.MultipartFile;
import ru.kabor.demand.prediction.entity.Request;
import ru.kabor.demand.prediction.utils.exceptions.InvalidHeaderException;

import java.io.IOException;
import java.util.Map;

/** It contains methods for working with requests to server */
public interface RequestService {
	
    /** Create request in db.
     * @param reqParams parameters of request
     * @param documentPath file
     * @return request object
     * @throws DataServiceException
     */
    Request createRequest(Map<String, String[]> reqParams, MultipartFile documentPath) throws DataServiceException;   
    
    /** Validate file and save request to db.
     * @param file file
     * @param reqParams  parameters of request
     * @return request object
     * @throws InvalidHeaderException
     * @throws IOException
     * @throws InvalidFormatException
     * @throws DataServiceException
     */
    Request addNewRequest(MultipartFile file, Map<String, String[]> reqParams)
            throws InvalidHeaderException, IOException, InvalidFormatException, DataServiceException;

    /**read request from db and parses its data to v_sales_rest
     * @return request object
     * @throws IOException
     * @throws InvalidFormatException
     */
    Request importRawRequest() throws IOException, InvalidFormatException;
    
    /** make prediction by data presented in tables v_forecast_parameter and v_sales_rest
     * @param requestId id of request
     * @return filename
     * @throws DataServiceException
     */
    String makeRequestPrediction(Integer requestId) throws DataServiceException;
    
    /** calculate elasticity by data presented in table v_sales_rest with prices
     * @param requestId id of request
     * @return filename
     * @throws DataServiceException
     */
    String makeRequestElasticity(Integer requestId) throws DataServiceException;

	/** make prediction and calculates elasticity by data presented in tables v_forecast_parameter and v_sales_rest
	 * @param requestId id of request
	 * @return  filename
	 * @throws DataServiceException
	 */
	String makeRequestAndElasticityPrediction(Integer requestId) throws DataServiceException;
}
