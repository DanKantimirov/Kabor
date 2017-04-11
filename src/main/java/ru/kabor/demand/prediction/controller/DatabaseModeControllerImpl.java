package ru.kabor.demand.prediction.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.kabor.demand.prediction.email.EmailSender;
import ru.kabor.demand.prediction.email.EmailSenderException;
import ru.kabor.demand.prediction.entity.RequestElasticityParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponceElasticity;
import ru.kabor.demand.prediction.entity.ResponceForecast;
import ru.kabor.demand.prediction.service.DataService;
import ru.kabor.demand.prediction.service.DataServiceException;

/** Implementation of DatabaseModeController */
@RestController
@Secured("ADMINISTRATOR")
public class DatabaseModeControllerImpl implements DatabaseModeController {
	
	@Autowired
    private DataService dataService;
	
    @Value("${storage.outputFolderLocation}")
    private String outputFolderLocation;
	
	@Autowired
	EmailSender emailSender;
    
	private static final Logger LOG = LoggerFactory.getLogger(DatabaseModeControllerImpl.class);
	
	@Override
	@RequestMapping(value = "/forecastsingle",consumes=MediaType.APPLICATION_JSON_VALUE )
	public @ResponseBody String getForecastSingle(@RequestBody RequestForecastParameterSingle forecastParameterSingle, 
			HttpServletRequest request,
			HttpServletResponse response,
    		RedirectAttributes redirectAttributes) {
		ResponceForecast forecastResponse;
		try {
			forecastResponse = dataService.getForecastSingleDatabaseMode(forecastParameterSingle);
			String filePath = dataService.createForecastResultFileSingleDatabaseMode(forecastResponse);
			LOG.info("created: " + filePath);
			return filePath;
		} catch (DataServiceException e) {
			LOG.error(e.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return e.toString();
		}
	}
	
	@Override
	@RequestMapping(value = "/forecastmultiple",consumes=MediaType.APPLICATION_JSON_VALUE )
	public String getForecastMultiple(@RequestBody RequestForecastParameterMultiple forecastParameterMultiple,
			HttpServletRequest request,
			HttpServletResponse response,
    		RedirectAttributes redirectAttributes){
		try {
			List<ResponceForecast> forecastResponse = dataService.getForecastMultipleDatabaseMode(forecastParameterMultiple);
			String filePath = dataService.createForecastResultFileMultipleDatabaseMode(forecastResponse);
			LOG.info("created: " + filePath);
			return filePath;
		} catch (DataServiceException e) {
			LOG.error(e.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return e.toString();
		}
	}
	
	@Override
	@RequestMapping(value = "/elasticitymultiple",consumes=MediaType.APPLICATION_JSON_VALUE )
	public String getElasticityMultiple(@RequestBody RequestElasticityParameterMultiple elasticityParameterMultiple,
			HttpServletRequest request,
			HttpServletResponse response,
    		RedirectAttributes redirectAttributes){
		try {
			List<ResponceElasticity> elasticityResponse = dataService.getElasticityMultipleDatabaseMode(elasticityParameterMultiple);
			String filePath = dataService.createElasticityResultFileMultipleDatabaseMode(elasticityResponse);
			LOG.info("created: " + filePath);
			if (elasticityParameterMultiple.getIsSendByEmail().equals(true)) {
				try {
					emailSender.sendMessageWithElasticityResult(Long.valueOf(elasticityParameterMultiple.getRequestId()), elasticityParameterMultiple.getEmail(), filePath);
				} catch (MessagingException | EmailSenderException e) {
					LOG.error("ERROR can't send email: " + e.toString());
				}
			}
			return filePath;
		} catch (DataServiceException e) {
			LOG.error(e.toString());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return e.toString();
		}
	}
	
	@RequestMapping(value = "/report/{fileName:.+}", method = RequestMethod.GET)
    public void downloadPDFResource(@PathVariable("fileName") String fileName, 
    		HttpServletRequest request,
			HttpServletResponse response,
    		RedirectAttributes redirectAttributes){
        Path file = Paths.get(outputFolderLocation, fileName);
        if (Files.exists(file)){
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.addHeader("Content-Disposition", "attachment; filename="+fileName);
            try {
                Files.copy(file, response.getOutputStream());
                response.getOutputStream().flush();
            } 
            catch (IOException ex) {
            	LOG.warn("Some problems with IE");
            }
        } else{
        	LOG.error("Can't find file:" + fileName);
        	try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Can't find file:" + fileName);
			} catch (IOException e) {
				LOG.error(e.toString());
			}
        }
        
    }
}
