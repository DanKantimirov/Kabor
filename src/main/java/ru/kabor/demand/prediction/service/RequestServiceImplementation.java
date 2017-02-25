package ru.kabor.demand.prediction.service;

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
import ru.kabor.demand.prediction.entity.ForecastParameter;
import ru.kabor.demand.prediction.entity.Request;
import ru.kabor.demand.prediction.entity.SalesRest;
import ru.kabor.demand.prediction.repository.ForecastParameterRepository;
import ru.kabor.demand.prediction.repository.RequestRepository;
import ru.kabor.demand.prediction.utils.ConstantUtils;
import ru.kabor.demand.prediction.utils.ExcelUtils;
import ru.kabor.demand.prediction.utils.FORECAST_METHOD;
import ru.kabor.demand.prediction.utils.SMOOTH_TYPE;
import ru.kabor.demand.prediction.utils.exceptions.InvalidHeaderException;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static ru.kabor.demand.prediction.utils.ConstantUtils.PARSE_EXCEL_SALES_REST_LIST_SIZE;
import static ru.kabor.demand.prediction.utils.ExcelUtils.readValueFromXls;

@Service
public class RequestServiceImplementation implements RequestService {

    private static final Logger LOG = LoggerFactory.getLogger(RequestServiceImplementation.class);

    @Autowired
    RequestRepository requestRepository;
    
    @Autowired
    ForecastParameterRepository forecastParameterRepository;

    @Autowired
    private DataService dataService;

    @Autowired
    private SalesRestService salesRestService;

    java.text.SimpleDateFormat simpleDateTimeFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Request createRequest(Map<String, String[]> reqParams, MultipartFile file) throws DataServiceException {
        LOG.debug("prepare request for saving to db ");

        String fileName = dataService.putFile(file);
        
        Request request = new Request();
        request.setDocumentPath(fileName);
        request.setEmail(reqParams.get("inputEmail")[0]);
        request.setStatus(ConstantUtils.REQUEST_ADDED);
        request.setSendDateTime(LocalDateTime.now());
        request = requestRepository.saveAndFlush(request);
        
        ForecastParameter forecastParameter = new ForecastParameter();
        String defaultSettingsInput = reqParams.get("defaultSettingsInput")[0];
        if(defaultSettingsInput!=null && defaultSettingsInput.equals("1")){
        	forecastParameter.setDuration(7);
        	forecastParameter.setForecast_method(FORECAST_METHOD.WINTER_HOLT);
        	forecastParameter.setSmoothing_method(SMOOTH_TYPE.NO);
        } else{
        	Integer duration = Integer.valueOf(reqParams.get("predictionDaysInput")[0]);
        	FORECAST_METHOD method = FORECAST_METHOD.valueOf(reqParams.get("predictionMethod")[0]);
        	SMOOTH_TYPE smooth = SMOOTH_TYPE.valueOf(reqParams.get("useSmoothInput")[0]);
        	forecastParameter.setDuration(duration);
        	forecastParameter.setForecast_method(method);
        	forecastParameter.setSmoothing_method(smooth);
        }
        forecastParameter.setRequest(request);											//Try to make it in one transaction with creating request
        forecastParameterRepository.save(forecastParameter);

        LOG.debug("new request successfully saved to db");

        return request;
    }

    @Override
    public Request addNewRequest(MultipartFile file, Map<String, String[]> reqParams)
            throws InvalidHeaderException, IOException, InvalidFormatException, DataServiceException {
        LOG.debug("processing new request. validate ans save");
        ExcelUtils.validateCsvHeaders(file);
        return createRequest(reqParams, file);
    }

    @Override
    public void importRawRequest() throws IOException, InvalidFormatException {
        LOG.debug("ready for read request for parsing from db");
        //red one request where status 0
        Request request = requestRepository.findByStatus(ConstantUtils.REQUEST_ADDED);

        //we have not processed requests
        if (request != null) {
            LOG.debug("got request for parsing. preparing");

            try {
                Path file = dataService.getStorageInputFilePath(request.getDocumentPath());
                Workbook workbook = WorkbookFactory.create(file.toFile());

                Sheet sheet = workbook.getSheetAt(0);
                Iterator<Row> iterator = sheet.rowIterator();
                int rowCounter = 0;
                List<SalesRest> saleRestList = new ArrayList<>();

                while (iterator.hasNext()) {
                    LOG.debug("processing workbook. row #%d", rowCounter);

                    Row row = iterator.next();

                    //ignore header
                    if (rowCounter == 0) {
                        rowCounter++;
                        continue;
                    }

                    SalesRest saleRest = new SalesRest();
                    saleRest.setRequest(request);
                    saleRest.setWhsId(Integer.parseInt(readValueFromXls(workbook, row, 1)));
                    saleRest.setArtId(Integer.parseInt(readValueFromXls(workbook, row, 2)));
                    //check work with date and parse locales
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
                    saleRest.setDayId(LocalDate.parse(readValueFromXls(workbook, row, 3), formatter));
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
                workbook.close();

                request.setStatus(ConstantUtils.REQUEST_PROCESSED);
                requestRepository.saveAndFlush(request);

                LOG.debug("all rows request #%d successfully savedto db", request.getId());

            } catch (Exception exception) {
                request.setStatus(ConstantUtils.IMPORT_ERROR);
                requestRepository.saveAndFlush(request);
                throw exception;
            }
        }
        LOG.debug("request parse procedure finished");
    }
}
