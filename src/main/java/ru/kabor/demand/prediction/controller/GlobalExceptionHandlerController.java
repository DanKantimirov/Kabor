package ru.kabor.demand.prediction.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/** Handler for all exceptions in controllers */
@ControllerAdvice  
@RestController 
public class GlobalExceptionHandlerController {
	private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandlerController.class);
	
	@ExceptionHandler(Exception.class)
	public ModelAndView handleError(HttpServletRequest req, Exception exception) {
		LOG.error("Request: " + req.getRequestURL(), exception);
		ModelAndView mav = new ModelAndView("error");
		mav.addObject("exception", exception);
		mav.addObject("url", req.getRequestURL());
		return mav;
	}
}
