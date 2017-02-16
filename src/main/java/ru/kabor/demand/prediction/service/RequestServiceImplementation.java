package ru.kabor.demand.prediction.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.kabor.demand.prediction.model.Request;
import ru.kabor.demand.prediction.repository.RequestRepository;
import ru.kabor.demand.prediction.repository.SalesRestRepository;
import ru.kabor.demand.prediction.utils.ConstantUtils;
import ru.kabor.demand.prediction.utils.ExcelUtils;
import ru.kabor.demand.prediction.utils.exceptions.InvalidHeaderException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class RequestServiceImplementation implements RequestService {

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    SalesRestRepository salesRestRepository;

    @Override
    public void createRequest(Map<String, String[]> reqParams, String documentPath) {
        Request request = new Request();
        request.setDocumentPath(documentPath);
        request.setEmail(reqParams.get("inputEmail")[0]);
        request.setStatus(ConstantUtils.REQUEST_ADDED);
        request.setSendDateTime(LocalDateTime.now());
        requestRepository.save(request);
    }

    @Override
    public void processNewRequest(MultipartFile file, Map<String, String[]> reqParams)
            throws InvalidHeaderException, IOException, InvalidFormatException {
        Workbook workbook = WorkbookFactory.create(file.getInputStream());

        ExcelUtils.validateCsvHeaders(workbook);

        createRequest(reqParams, file.getOriginalFilename());
    }
}
