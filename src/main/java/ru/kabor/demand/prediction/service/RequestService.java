package ru.kabor.demand.prediction.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.web.multipart.MultipartFile;
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
    void createRequest(Map<String, String[]> reqParams, String documentPath);

    /**
     * Validate file and save request to db.
     * @param file
     * @param reqParams
     * @throws InvalidHeaderException
     * @throws IOException
     * @throws InvalidFormatException
     */
    void addNewRequest(MultipartFile file, Map<String, String[]> reqParams)
            throws InvalidHeaderException, IOException, InvalidFormatException;

    /**
     * read request from db and parse it values
     * @throws IOException
     * @throws InvalidFormatException
     */
    void importRawRequest() throws IOException, InvalidFormatException;
}
