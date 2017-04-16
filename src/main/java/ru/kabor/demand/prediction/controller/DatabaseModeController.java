package ru.kabor.demand.prediction.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.kabor.demand.prediction.entity.RequestElasticityParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastAndElasticityParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;

/** Controller for working when we take data(sales,rest,price) from database*/
public interface DatabaseModeController {
	
	/** Create forecast for 1 shop and 1 product 
	 * @param forecastParameterSingle parameter for making forecast
	 * @param request http request from client
	 * @param response http response to client
	 * @param redirectAttributes attributes of redirect 
	 * @return result file name */
	public String getForecastSingle(RequestForecastParameterSingle forecastParameterSingle, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes);
	
	/** Create forecast for many shops and many products  
	 * @param forecastParameter parameter for making forecast
	 * @param request http request from client
	 * @param response http response to client
	 * @param redirectAttributes attributes of redirect 
	 * @return result file name */
	public String getForecastMultiple(RequestForecastParameterMultiple forecastParameter,  HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes);
	
	
	/** Calculate elasticity for many shops and many products 	 
	 * @param elasticityParameter parameter for calculating elasticity
	 * @param request http request from client
	 * @param response http response to client
	 * @param redirectAttributes attributes of redirect 
	 * @return result file name */
	public String getElasticityMultiple(RequestElasticityParameterMultiple elasticityParameter,  HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes);
	
	
	/** Create forecast and elasticity for many shops and many products  
	 * @param forecastAndElasticityParameter parameter for making forecast and calculating elasticity
	 * @param request request from client
	 * @param response response to client
	 * @param redirectAttributes attributes of redirect 
	 * @return result file name
	 */
	public String getForecastAndElasticityMultiple(RequestForecastAndElasticityParameterMultiple forecastAndElasticityParameter,  HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes);
	
	/** Return full link to Excel with result  
	 * @param fileName name of the file in output directory
	 * @param request  http request from client
	 * @param response http response to client
	 * @param redirectAttributes attributes of redirect 
	 * @return file in attachment
	 */
	public void downloadPDFResource(String fileName,  HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes);
}
