package ru.kabor.demand.prediction.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.web.multipart.MultipartFile;
import ru.kabor.demand.prediction.entity.Request;
import ru.kabor.demand.prediction.utils.exceptions.InvalidHeaderException;

import java.io.IOException;
import java.util.Map;

/** It contains methods for working with requests to server */
public interface RequestService {

    /**
     * Create request in db.
     * @param reqParams
     * @param documentPath
     */
    Request createRequest(Map<String, String[]> reqParams, MultipartFile documentPath) throws DataServiceException;

    /**
     * Validate file and save request to db.
     * @param file
     * @param reqParams
     * @throws InvalidHeaderException
     * @throws IOException
     * @throws InvalidFormatException
     */
    Request addNewRequest(MultipartFile file, Map<String, String[]> reqParams)
            throws InvalidHeaderException, IOException, InvalidFormatException, DataServiceException;

    /**
     * It reads request from db and parses its data to v_sales_rest
     * @return imported request, null if nothing changed
     * @throws IOException
     * @throws InvalidFormatException
     */
    Request importRawRequest() throws IOException, InvalidFormatException;
    
    /**
     * It makes prediction by data presented in tables v_forecast_parameter and v_sales_rest
     * @return path to xls file with result
     * */
    String makeRequestPrediction(Integer requestId) throws DataServiceException;
    
    /**
     * It calculates elasticity by data presented in table v_sales_rest with prices
     * @return path to xls file with result
     * */
    String makeRequestElasticity(Integer requestId) throws DataServiceException;

    /**
     * It makes prediction and calculates elasticity by data presented in tables v_forecast_parameter and v_sales_rest
     * @return path to xls file with result
     * @throws DataServiceException 
     * */
	String makeRequestAndElasticityPrediction(Integer requestId) throws DataServiceException;
}
