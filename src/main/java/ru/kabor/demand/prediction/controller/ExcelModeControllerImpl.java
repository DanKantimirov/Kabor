package ru.kabor.demand.prediction.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.kabor.demand.prediction.email.EmailSender;
import ru.kabor.demand.prediction.email.EmailSenderException;
import ru.kabor.demand.prediction.entity.Request;
import ru.kabor.demand.prediction.service.DataService;
import ru.kabor.demand.prediction.service.DataServiceException;
import ru.kabor.demand.prediction.service.RequestService;
import ru.kabor.demand.prediction.utils.FORECAST_METHOD;
import ru.kabor.demand.prediction.utils.SMOOTH_TYPE;
import ru.kabor.demand.prediction.utils.VerifyCaptcha;
import ru.kabor.demand.prediction.utils.exceptions.InvalidHeaderException;

/** Implementation of ExcelModeController*/
@Controller
public class ExcelModeControllerImpl implements ExcelModeController {
	
	@Autowired
    private DataService dataService;

	@Autowired
	RequestService requestService;

	@Autowired
	EmailSender emailSender;
	
	@Value("${serverUser.captchaKey}")
	String captchaSecretKey;
	
	@Value("${serverUser.useRealCaptch}")
	Boolean isUseRealCaptcha;
	
	@Value("${storage.maxFileSizeMB}")
	Integer maxFileSizeMB;
	
	private static final Logger LOG = LoggerFactory.getLogger(ExcelModeControllerImpl.class);
	
	@Override
    @GetMapping("/excelMode/uploadedFiles")
    public String listUploadedFiles(Model model) throws IOException, DataServiceException {
        model.addAttribute("files", dataService
                .getFilePathAllFromInputStorage()
                .map(path ->
                        MvcUriComponentsBuilder
                                .fromMethodName(ExcelModeControllerImpl.class, "serveFile", path.getFileName().toString())
                                .build().toString())
                .collect(Collectors.toList()));
        return "uploadFormForecast";
    }
    
	@Override
    @GetMapping("/excelMode")
    public String getUploadFormForecast(Model model){
        return "uploadFormForecast";
    }
    
    @Override
    @GetMapping("/excelModeElastic")
    public String getUploadElasticity(Model model){
        return "uploadElasticity";
    }
    
    @Override
    @GetMapping("excelMode/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws DataServiceException {
        Resource file = dataService.getFileFromInputStorageAsResourse(filename);
        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+file.getFilename()+"\"")
                .body(file);
    }
    
	@Override
    @GetMapping("excelMode/filesOutput/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFileOutput(@PathVariable String filename) throws DataServiceException {
        Resource file = dataService.getFileFromOutputStorageAsResourse(filename);
        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+file.getFilename()+"\"")
                .body(file);
    }

	@Override
    @PostMapping("/sendContactEmail")
    public String sendContactEmail(
			@RequestParam(name="firstname") String firstname,
			@RequestParam(name="lastname") String lastname,
			@RequestParam(name="email") String email,
			@RequestParam(name="comments") String comments
			) {
		emailSender.sendContactEmail(firstname, lastname, email, comments);
    	return "redirect:/contactUs.html";
	}

    @PostMapping("/excelMode")
    public String handleFileUpload(
    		@RequestParam(name="fileInput", required=true) MultipartFile file,
    		@RequestParam(name="elasticityTypeInput", required=false) String elasticityTypeInput,
    		@RequestParam(name="defaultSettingsInput", required=true) String defaultSettingsInput,
    		@RequestParam(name="predictionDaysInput", required=false) Integer forecastDuration,
    		@RequestParam(name="predictionMethod", required=false) FORECAST_METHOD forecastMethod,
    		@RequestParam(name="useSmoothInput", required=false) SMOOTH_TYPE smoothType,
    		@RequestParam(name="inputEmail", required=true) String email,
    		@RequestParam(name="g-recaptcha-response", required=true) String gRecaptchaResponse,
			HttpServletRequest request,
    		RedirectAttributes redirectAttributes) throws DataServiceException, UnsupportedEncodingException, IOException {
    	
    	boolean verify = VerifyCaptcha.verify(captchaSecretKey, gRecaptchaResponse, isUseRealCaptcha);
    	if(!verify){
    		throw new DataServiceException("Wrong captcha");
    	}
		try {
			if (file.getSize() > (maxFileSizeMB * 1024 * 1024)) {
				throw new IllegalArgumentException("Size of file too big!!!");
			}
			Map<String, String[]> requestParams = request.getParameterMap();
			Request reqEnt = requestService.addNewRequest(file, requestParams);
			emailSender.sendMessageRequestAdded((long) reqEnt.getId());
			redirectAttributes.addFlashAttribute("message", "We have already started forecast processing!"
			+ " You will receive a notification to email when the process is complete.");
			redirectAttributes.addFlashAttribute("color", "green");
		} catch (InvalidHeaderException e) {
			redirectAttributes.addFlashAttribute("message", "Format of your file is invalid! Please, check columns name.");
			redirectAttributes.addFlashAttribute("color", "red");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("message", "Your file is too big. Allowed only " + maxFileSizeMB + " MB.");
			redirectAttributes.addFlashAttribute("color", "red");
		} catch (InvalidFormatException e) {
			redirectAttributes.addFlashAttribute("message", "Unable process your file. Perhaps it has been broken.");
			redirectAttributes.addFlashAttribute("color", "red");
		} catch (MessagingException e) {
			redirectAttributes.addFlashAttribute("message", "Your file is uploaded. You will get result soon. Can't send email.");
			redirectAttributes.addFlashAttribute("color", "orange");
		} catch (EmailSenderException e) {
			LOG.error("Can't send email", e);
		}
		return "redirect:/excelMode";
    }
    
    @Override
    @PostMapping("/excelModeElastic")
    public String handleFileUploadElasticity(
    		@RequestParam(name="fileInput", required=true) MultipartFile file,
    		@RequestParam(name="inputEmail", required=true) String email,
    		@RequestParam(name="g-recaptcha-response", required=true) String gRecaptchaResponse,
			HttpServletRequest request,
    		RedirectAttributes redirectAttributes) throws DataServiceException, UnsupportedEncodingException, IOException {
    	
    	boolean verify = VerifyCaptcha.verify(captchaSecretKey, gRecaptchaResponse, isUseRealCaptcha);
    	if(!verify){
    		throw new DataServiceException("Wrong captcha");
    	}
		try {
			if (file.getSize() > (maxFileSizeMB * 1024 * 1024)) {
				throw new IllegalArgumentException("Size of file too big!!!");
			}
			Map<String, String[]> requestParams = request.getParameterMap();
			requestParams.put("elasticityTypeInput", new String[] { "ELASTICITY" });
			Request reqEnt = requestService.addNewRequest(file, requestParams);
			emailSender.sendMessageRequestAdded((long) reqEnt.getId());
			redirectAttributes.addFlashAttribute("message",
					"We have already started calculating elasticity!" + " You will receive a notification to email when the process is complete.");
			redirectAttributes.addFlashAttribute("color", "green");
		} catch (InvalidHeaderException e) {
			redirectAttributes.addFlashAttribute("message", "Format of your file is invalid! Please, check columns name.");
			redirectAttributes.addFlashAttribute("color", "red");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("message", "Your file is too big. Allowed only " + maxFileSizeMB + " MB.");
			redirectAttributes.addFlashAttribute("color", "red");
		} catch (InvalidFormatException e) {
			redirectAttributes.addFlashAttribute("message", "Unable process your file. Perhaps it has been broken.");
			redirectAttributes.addFlashAttribute("color", "red");
		} catch (MessagingException e) {
			redirectAttributes.addFlashAttribute("message", "Your file is uploaded. You will get result soon. Can't send email.");
			redirectAttributes.addFlashAttribute("color", "orange");
		} catch (EmailSenderException e) {
			LOG.error("Can't send email", e);
		}
		return "redirect:/excelModeElastic";
    }
}
