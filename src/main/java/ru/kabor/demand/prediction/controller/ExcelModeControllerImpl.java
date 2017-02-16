package ru.kabor.demand.prediction.controller;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kabor.demand.prediction.service.DataService;
import ru.kabor.demand.prediction.service.DataServiceException;
import ru.kabor.demand.prediction.service.RequestService;
import ru.kabor.demand.prediction.utils.FORECAST_METHOD;
import ru.kabor.demand.prediction.utils.SMOOTH_TYPE;
import ru.kabor.demand.prediction.utils.VerifyCaptcha;
import ru.kabor.demand.prediction.utils.exceptions.InvalidHeaderException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;

@Controller
public class ExcelModeControllerImpl implements ExcelModeController {
	
	@Autowired
    private DataService dataService;

	@Autowired
	RequestService requestService;

	private static final Logger LOG = LoggerFactory.getLogger(ExcelModeControllerImpl.class);
	
	/** Return list of already downloaded files and redirect to form of adding new
	 * @throws IOException
	 * @throws DataServiceException*/
    @GetMapping("/excelMode")
    public String listUploadedFiles(Model model) throws IOException, DataServiceException {
        model.addAttribute("files", dataService
                .getStorageFilePathAll()
                .map(path ->
                        MvcUriComponentsBuilder
                                .fromMethodName(ExcelModeControllerImpl.class, "serveFile", path.getFileName().toString())
                                .build().toString())
                .collect(Collectors.toList()));
        return "uploadForm";
    }
    
	/** Return file by path
	 * @throws DataServiceException*/
    @GetMapping("excelMode/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws DataServiceException {
        Resource file = dataService.getStorageFileAsResourse(filename);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+file.getFilename()+"\"")
                .body(file);
    }

	/** Send file to server and start making forecasting
	 * @throws DataServiceException
	 * @throws IOException 
	 * @throws UnsupportedEncodingException */
    @PostMapping("/excelMode")
    public String handleFileUpload(
    		@RequestParam("fileInput") MultipartFile file, 
    		@RequestParam("dateStartInput") String trainingStart,
    		@RequestParam("dateEndInput") String trainingEnd,
    		@RequestParam("predictionDaysInput") Integer forecastDuration,
    		@RequestParam("predictionMethod") FORECAST_METHOD forecastMethod,
    		@RequestParam("useSmoothInput") SMOOTH_TYPE smoothType,
    		@RequestParam("inputEmail") String email,
    		@RequestParam("g-recaptcha-response") String gRecaptchaResponse,
			HttpServletRequest request,
    		RedirectAttributes redirectAttributes) throws DataServiceException, UnsupportedEncodingException, IOException {
    	
    	String testSecretKey = "6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe";
    	boolean verify = VerifyCaptcha.verify(testSecretKey, gRecaptchaResponse, false);
    	if(!verify){
    		throw new DataServiceException("Wrong captcha");
    	}
		try {
			requestService.processNewRequest(file, request.getParameterMap());

			dataService.putFile(file);

			redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");

		} catch (InvalidHeaderException exception) {
			redirectAttributes.addFlashAttribute("message", "Your file format is invalid. Check file columns name.");
		} catch (InvalidFormatException exception) {
			redirectAttributes.addFlashAttribute("message", "Unable process your file. Perhaps it has been broken.");
		}

		// respond to user as quickly as possible.
		// in html page

        return "redirect:/excelMode";
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
