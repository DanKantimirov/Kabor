package ru.kabor.demand.prediction.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.kabor.demand.prediction.entity.RequestForecastParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.service.DataServiceException;

/** Controller for working when we take data(sales,rest) from database*/
public interface DatabaseModeController {
	
	/** Create forecast for 1 shop and 1 SKU
	 * @throws Exception*/
	public String getForecastSingle(RequestForecastParameterSingle forecastParameter) throws Exception;
	/** Create forecast for many shops and many SKUs
	 * @throws Exception*/
	public String getForecastMultiple(RequestForecastParameterMultiple forecastParameter) throws Exception;
	/** Return link to Excel with result
	 * @throws DataServiceException */
	public void downloadPDFResource(HttpServletRequest request, HttpServletResponse response, String fileName) throws DataServiceException;
}
