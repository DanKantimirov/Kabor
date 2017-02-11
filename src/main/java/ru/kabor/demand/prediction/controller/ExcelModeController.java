package ru.kabor.demand.prediction.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
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
	public String listUploadedFiles(Model model) throws IOException, DataServiceException;
	/** Return file by path
	 * @throws DataServiceException*/
	public ResponseEntity<Resource> serveFile(String filename) throws DataServiceException;

	/**
	 * Send file to server and start making forecasting
	 * 
	 * @throws DataServiceException
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 */
	public String handleFileUpload(MultipartFile file, String trainingStart, String trainingEnd,
			Integer forecastDuration, FORECAST_METHOD forecastMethod, SMOOTH_TYPE smoothType,String gRecaptchaResponse,
			String email, RedirectAttributes redirectAttributes) throws DataServiceException, UnsupportedEncodingException, IOException;
}
