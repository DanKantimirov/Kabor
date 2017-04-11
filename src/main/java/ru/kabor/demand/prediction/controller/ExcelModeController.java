package ru.kabor.demand.prediction.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.kabor.demand.prediction.service.DataServiceException;
import ru.kabor.demand.prediction.utils.FORECAST_METHOD;
import ru.kabor.demand.prediction.utils.SMOOTH_TYPE;

/** Controller for working when we take data(sales,rest) from Excel document*/
public interface ExcelModeController {
	
	/** Return list of already downloaded files and redirect to form of adding new
	 * @throws IOException
	 * @throws DataServiceException*/
	String listUploadedFiles(Model model) throws IOException, DataServiceException;
	
	/** Return file by path
	 * @param filename name of file
	 * @throws DataServiceException*/
	ResponseEntity<Resource> serveFile(String filename) throws DataServiceException;

	/** Send file to server and start making forecasting
	 * @param file Excel file with sales and rests
	 * @param defaultSettingsInput If it is not null or 0: 7 days, WINTER_HOLT, none smoothing
	 * @param forecastDuration Duration of forecast
	 * @param forecastMethod Method of forecasting
	 * @param smoothType Method of smoothing raw data
	 * @param email User's email
	 * @param gRecaptchaResponse Response from captcha
	 * @throws DataServiceException
	 * @throws IOException 
	 * @throws UnsupportedEncodingException */
	String handleFileUpload(MultipartFile file, String elasticityTypeInput, String defaultSettingsInput,
		   Integer forecastDuration, FORECAST_METHOD forecastMethod, SMOOTH_TYPE smoothType, String gRecaptchaResponse,
		   String email, HttpServletRequest request, RedirectAttributes redirectAttributes)
					throws DataServiceException, UnsupportedEncodingException, IOException;
	
	/** Return uploadFormForecast.html */
	String getUploadFormForecast(Model model);
	
	/** Return uploadElasticity.html */
	String getUploadElasticity(Model model);
	
	/** Send file to server and start calculating elasticity
	 * @param file Excel file with sales and rests
	 * @param email User's email
	 * @param gRecaptchaResponse Response from captcha
	 * @throws DataServiceException
	 * @throws IOException 
	 * @throws UnsupportedEncodingException */
    String handleFileUploadElasticity(MultipartFile file, String email, String gRecaptchaResponse,
			HttpServletRequest request,	RedirectAttributes redirectAttributes) throws DataServiceException, UnsupportedEncodingException, IOException;
   
    /** Return file by path from output storage
	 * @throws DataServiceException*/
    ResponseEntity<Resource> serveFileOutput(@PathVariable String filename) throws DataServiceException;
    
    /**
	 * Send user comment to our email
	 * @param firstname	firstname
	 * @param lastname	lastname
	 * @param email	email
	 * @param comments	comment
	 * @return
	 */
    String sendContactEmail(String firstname, String lastname, String email, String comments);
}
