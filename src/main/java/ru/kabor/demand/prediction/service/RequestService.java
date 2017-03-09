package ru.kabor.demand.prediction.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.web.multipart.MultipartFile;
import ru.kabor.demand.prediction.entity.Request;
import ru.kabor.demand.prediction.utils.exceptions.InvalidHeaderException;

import java.io.IOException;
import java.util.Map;

/**
 * interface for managing request db entities
 */
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
     * @return id of proceed request, null if nothing changed
     * @throws IOException
     * @throws InvalidFormatException
     */
    Integer importRawRequest() throws IOException, InvalidFormatException;
    
    /**
     * It makes prediction by data presented in tables v_forecast_parameter and v_sales_rest
     * @return path to xls file with result
     * */
    String makeRequestPrediction(Integer requestId) throws DataServiceException;
}
