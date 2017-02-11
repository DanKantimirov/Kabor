package ru.kabor.demand.prediction.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.kabor.demand.prediction.service.DataService;
import ru.kabor.demand.prediction.service.DataServiceException;
import ru.kabor.demand.prediction.utils.FORECAST_METHOD;
import ru.kabor.demand.prediction.utils.SMOOTH_TYPE;
import ru.kabor.demand.prediction.utils.VerifyCaptcha;

@Controller
public class ExcelModeControllerImpl implements ExcelModeController {
	
	@Autowired
    private DataService dataService;
	
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
    		RedirectAttributes redirectAttributes) throws DataServiceException, UnsupportedEncodingException, IOException {
    	
    	String testSecretKey = "6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe";
    	boolean verify = VerifyCaptcha.verify(testSecretKey, gRecaptchaResponse, false);
    	if(!verify){
    		throw new DataServiceException("Wrong captcha");
    	}
        dataService.putFile(file);
        redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");
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
