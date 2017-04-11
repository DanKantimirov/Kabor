package ru.kabor.demand.prediction.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.kabor.demand.prediction.entity.RequestElasticityParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;

/** Controller for working when we take data(sales,rest,price) from database*/
public interface DatabaseModeController {
	
	/** Create forecast for 1 shop and 1 SKU  @throws Exception*/
	public String getForecastSingle(RequestForecastParameterSingle forecastParameterSingle, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes);
	/** Create forecast for many shops and many SKUs  @throws Exception*/
	public String getForecastMultiple(RequestForecastParameterMultiple forecastParameter,  HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes);
	/** Calculate elasticity for many shops and many SKUs @throws Exception*/
	public String getElasticityMultiple(RequestElasticityParameterMultiple elasticityParameter,  HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes);
	/** Return link to Excel with result  @throws DataServiceException */
	public void downloadPDFResource(String fileName,  HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes);
}
