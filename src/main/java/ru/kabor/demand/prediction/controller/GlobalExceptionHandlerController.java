package ru.kabor.demand.prediction.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/** Handler for all exceptions in another controllers */
@ControllerAdvice  
@RestController 
public class GlobalExceptionHandlerController {
	private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandlerController.class);
	
	/** Show error page with message
	 * @param req client request
	 * @param exception	occurred exception
	 * @return redirect to page with error message
	 */
	@ExceptionHandler(Exception.class)
	public ModelAndView handleError(HttpServletRequest req, Exception exception) {
		LOG.error("Request: " + req.getRequestURL(), exception);
		ModelAndView mav = new ModelAndView("error");
		mav.addObject("exception", exception);
		mav.addObject("url", req.getRequestURL());
		return mav;
	}
}
