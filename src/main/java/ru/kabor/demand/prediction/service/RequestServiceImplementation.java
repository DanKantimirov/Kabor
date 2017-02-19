package ru.kabor.demand.prediction.service;

import static ru.kabor.demand.prediction.utils.ConstantUtils.PARSE_EXCEL_SALES_REST_LIST_SIZE;
import static ru.kabor.demand.prediction.utils.ExcelUtils.readValueFromXls;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ru.kabor.demand.prediction.entity.Request;
import ru.kabor.demand.prediction.entity.SalesRest;
import ru.kabor.demand.prediction.repository.RequestRepository;
import ru.kabor.demand.prediction.utils.ConstantUtils;
import ru.kabor.demand.prediction.utils.ExcelUtils;
import ru.kabor.demand.prediction.utils.exceptions.InvalidHeaderException;

@Service
public class RequestServiceImplementation implements RequestService {

    private static final Logger LOG = LoggerFactory.getLogger(RequestServiceImplementation.class);

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    private DataService dataService;

    @Autowired
    private SalesRestService salesRestService;
    
    java.text.SimpleDateFormat simpleDateTimeFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void createRequest(Map<String, String[]> reqParams, String documentPath) {
        LOG.debug("prepare request for saving to db ");
        java.util.Date currentTime = new java.util.Date();
        Request request = new Request();
        request.setDocumentPath(documentPath);
        request.setEmail(reqParams.get("inputEmail")[0]);
        request.setStatus(ConstantUtils.REQUEST_ADDED);
        request.setSendDateTime(simpleDateTimeFormat.format(currentTime));
        requestRepository.save(request);
        LOG.debug("new request successfully saved to db");
    }

    @Override
    public void addNewRequest(MultipartFile file, Map<String, String[]> reqParams)
            throws InvalidHeaderException, IOException, InvalidFormatException {
        LOG.debug("processing new request. prepare workbook");
        Workbook workbook = WorkbookFactory.create(file.getInputStream());

        ExcelUtils.validateCsvHeaders(workbook);

        createRequest(reqParams, file.getOriginalFilename());
    }

    @Override
    public void importRawRequest() throws IOException, InvalidFormatException {
        LOG.debug("ready for read request for parsing from db");
        //red one request where status 0
        Request request = requestRepository.findByStatus(ConstantUtils.REQUEST_ADDED);

        //we have not processed requests
        if (request != null) {
            LOG.debug("got request for parsing. preparing");
            Path file = dataService.getStorageOutputFilePath(request.getDocumentPath());
            Workbook workbook = WorkbookFactory.create(file.toFile());

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = sheet.rowIterator();
            int rowCounter = 0;
            List<SalesRest> saleRestList = new ArrayList<>();

            while(iterator.hasNext()) {
                LOG.debug("processing workbook. row #%d", rowCounter);

                //ignore header
                if (rowCounter == 0) {
                    rowCounter++;
                    continue;
                }

                Row row = iterator.next();

                SalesRest saleRest = new SalesRest();
                saleRest.setRequest(request);
                saleRest.setWhsId(Integer.parseInt(readValueFromXls(workbook, row, 1)));
                saleRest.setArtId(Integer.parseInt(readValueFromXls(workbook, row, 2)));
                //check work with date and parse locales
                saleRest.setDayId(LocalDate.parse(readValueFromXls(workbook, row, 3)));
                saleRest.setSaleQnty(Double.parseDouble(readValueFromXls(workbook, row, 4)));
                saleRest.setRestQnty(Double.parseDouble(readValueFromXls(workbook, row, 5)));

                saleRestList.add(saleRest);

                if (rowCounter % PARSE_EXCEL_SALES_REST_LIST_SIZE == 0) {
                    LOG.debug("salesRest batch ready. saving it to db");
                    salesRestService.storeBathSalesRest(saleRestList);
                    saleRestList.clear();
                }
            }

            if (saleRestList.size() > 0) {
                salesRestService.storeBathSalesRest(saleRestList);
            }
            LOG.debug("all rows request #%d successfully savedto db", request.getId());
        }
        LOG.debug("request parse procedure finished");
    }
}
