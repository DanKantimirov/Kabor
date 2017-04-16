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
	
	/** Return list of already downloaded files (attribute files) and redirect to form of making forecast
	 * @param model model from Spring framework
	 * @return	redirect to page of making forecast
	 * @throws IOException
	 * @throws DataServiceException
	 */
	String listUploadedFiles(Model model) throws IOException, DataServiceException;
	

	/** Return file by name from input storage
	 * @param filename name of file
	 * @return http response with file
	 * @throws DataServiceException
	 */
	ResponseEntity<Resource> serveFile(String filename) throws DataServiceException;
	
    /** Return file by name from output storage
     * @param filename name of file
     * @return http response with file
     * @throws DataServiceException
     */
    ResponseEntity<Resource> serveFileOutput(@PathVariable String filename) throws DataServiceException;


	/** Send file to server and start making forecasting
	 * @param file file Excel file with sales and rests
	 * @param elasticityTypeInput should calculate elasticity
	 * @param defaultSettingsInput  if it is not null or 0: 7 days, WINTER_HOLT, none smoothing
	 * @param forecastDuration duration of the forecast
	 * @param forecastMethod method of forecasting
	 * @param smoothType method of smoothing raw data
	 * @param gRecaptchaResponse response from captcha
	 * @param email user's email
	 * @param request request http request from client
	 * @param redirectAttributes attributes of redirect 
	 * @return redirect to page of making forecast
	 * @throws DataServiceException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	String handleFileUpload(MultipartFile file, String elasticityTypeInput, String defaultSettingsInput,
		   Integer forecastDuration, FORECAST_METHOD forecastMethod, SMOOTH_TYPE smoothType, String gRecaptchaResponse,
		   String email, HttpServletRequest request, RedirectAttributes redirectAttributes)
					throws DataServiceException, UnsupportedEncodingException, IOException;
	
	/** Return uploadFormForecast.html
	 * @param model model from Spring framework
	 * @return redirect to uploadFormForecast.html
	 */
	String getUploadFormForecast(Model model);
	
	/** Return uploadElasticity.html
	 * @param model model from Spring framework
	 * @return redirect to uploadElasticity.html
	 */
	String getUploadElasticity(Model model);
		
    /** Send file to server and start calculating elasticity
     * @param file excel file with sales, rests and prices
     * @param email user's email
     * @param gRecaptchaResponse response from captcha
     * @param request http request from client
     * @param redirectAttributes attributes of redirect 
     * @return redirect to page of calculating elasticity
     * @throws DataServiceException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    String handleFileUploadElasticity(MultipartFile file, String email, String gRecaptchaResponse,
			HttpServletRequest request,	RedirectAttributes redirectAttributes) throws DataServiceException, UnsupportedEncodingException, IOException;
    
    /**  Send user's comment to service's email address
     * @param firstname firstname
     * @param lastname lastname
     * @param email email
     * @param comments comment
     * @return redirect to contactUs page
     */
    String sendContactEmail(String firstname, String lastname, String email, String comments);
}
