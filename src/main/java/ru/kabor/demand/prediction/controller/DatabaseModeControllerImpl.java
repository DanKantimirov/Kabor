package ru.kabor.demand.prediction.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import ru.kabor.demand.prediction.entity.RequestForecastParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponceForecast;
import ru.kabor.demand.prediction.service.DataService;
import ru.kabor.demand.prediction.service.DataServiceException;

@RestController
public class DatabaseModeControllerImpl implements DatabaseModeController {
	
	@Autowired
    private DataService dataService;
	
    @Value("${storage.outputFolderLocation}")
    private String outputFolderLocation;
	
	private static final Logger LOG = LoggerFactory.getLogger(DatabaseModeControllerImpl.class);
	
	/** Create forecast for 1 shop and 1 SKU
	 * @throws Exception*/
	@Override
	@RequestMapping(value = "/forecastsingle",consumes=MediaType.APPLICATION_JSON_VALUE )
	public @ResponseBody String getForecastSingle(@RequestBody RequestForecastParameterSingle forecastParameterSingle) throws Exception {
		ResponceForecast forecastResponse =  dataService.getForecastSingle(forecastParameterSingle);
		String filePath = dataService.getForecastFileSingle(forecastResponse);
		return filePath;
	}
	
	/** Create forecast for many shops and many SKUs
	 * @throws Exception*/
	@Override
	@RequestMapping(value = "/forecastmultiple",consumes=MediaType.APPLICATION_JSON_VALUE )
	public String getForecastMultiple(@RequestBody RequestForecastParameterMultiple forecastParameterMultiple) throws Exception {
		List<ResponceForecast> forecastResponse =  dataService.getForecastMultiple(forecastParameterMultiple);
		String filePath = dataService.getForecastFileMultiple(forecastResponse);
		return filePath;
	}
	
	/** Return link to Excel with result
	 * @throws DataServiceException */
	@RequestMapping("/report/{fileName:.+}")
    public void downloadPDFResource( HttpServletRequest request, 
                                     HttpServletResponse response, 
                                     @PathVariable("fileName") String fileName) throws DataServiceException 
    {
        Path file = Paths.get(outputFolderLocation, fileName);
        if (Files.exists(file)) 
        {
            response.setContentType("application/vnd.ms-excel");
            response.addHeader("Content-Disposition", "attachment; filename="+fileName);
            try
            {
                Files.copy(file, response.getOutputStream());
                response.getOutputStream().flush();
            } 
            catch (IOException ex) {
            	LOG.warn("Some problems with IE");
            }
        } else{
        	throw new DataServiceException("Can't find file:" + fileName);
        }
        
    }
	
	/** Handle all exceptions*/
	@ExceptionHandler(Exception.class)
	public ModelAndView handleError(HttpServletRequest req, Exception ex) {
		LOG.error("Request: " + req.getRequestURL() + " raised " + ex);
		ModelAndView mav = new ModelAndView();
		mav.addObject("exception", ex);
		mav.addObject("url", req.getRequestURL());
		mav.setViewName("error");
		return mav;
	}

}
