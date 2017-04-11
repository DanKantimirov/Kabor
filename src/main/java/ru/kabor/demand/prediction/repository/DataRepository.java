package ru.kabor.demand.prediction.repository;

import java.util.Date;
import java.util.List;

import org.springframework.jdbc.support.rowset.SqlRowSet;

import ru.kabor.demand.prediction.entity.RequestElasticityParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestElasticityParameterSingle;
import ru.kabor.demand.prediction.entity.RequestForecastParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponceElasticity;
import ru.kabor.demand.prediction.entity.ResponceForecast;
import ru.kabor.demand.prediction.entity.ResponceForecastAndElasticity;
import ru.kabor.demand.prediction.service.DataServiceException;
import ru.kabor.demand.prediction.entity.RequestForecastAndElasticityParameterSingle;

/** It contains methods for working with database and calling rUtils */
public interface DataRepository {
	
	// ===========================Sales and Prices===========================================
	/** getting whs_id, art_id, sale_qnty, rest_qnty, day_id, price from v_sales_rest */
	SqlRowSet getSalesWithPrices(RequestForecastParameterSingle forecastParameters) throws DataServiceException;
	/** getting whs_id, art_id, sale_qnty, rest_qnty, day_id, price from v_sales_rest */
	SqlRowSet getSalesMultipleWithPrices(RequestForecastParameterMultiple forecastParameters) throws DataServiceException;
	/** getting whs_id, art_id, sale_qnty, rest_qnty, day_id, price from v_sales_rest */
	SqlRowSet getSalesWithPrices(RequestElasticityParameterSingle elasticityParameter) throws DataServiceException;
	/** getting whs_id, art_id, sale_qnty, rest_qnty, day_id, price from v_sales_rest */
	SqlRowSet getSalesMultipleWithPrices(RequestElasticityParameterMultiple elasticityParameterMultiple) throws DataServiceException;
	
	// ===========================Forecase and Elasticity for Excel mode=======================
	/** calling rUtils for getting forecast */
	ResponceForecast getForecast(RequestForecastParameterSingle forecastParameters, SqlRowSet salesRowSet) throws DataServiceException;
	/** calling rUtils for calculating elasticity */
	ResponceElasticity getElasticity(RequestElasticityParameterSingle elasticityParameter, SqlRowSet salesRowSet) throws DataServiceException;
	/** calling rUtils for getting forecast and calculating elasticity */
	ResponceForecastAndElasticity getForecastAndElasticity(RequestForecastAndElasticityParameterSingle requestParameter, SqlRowSet salesRowSet) throws DataServiceException;
	
	// ===========================Forecase and Elasticity for Database mode=====================
	/** calling rUtils by MultithreadingForecastCallable for getting forecast */
	List<ResponceForecast> getForecastMultiple(RequestForecastParameterMultiple forecastParameters, SqlRowSet salesRowSet) throws DataServiceException;
	/** calling rUtils by MultithreadingElasticityCallable for getting forecast */
	List<ResponceElasticity> getElasticityMultiple(RequestElasticityParameterMultiple elasticityParameterMultiple, SqlRowSet salesRowSet) throws DataServiceException;
	
	// ===========================Request parameters============================================
	/** making list of request forecast parameters from v_forecast_parameter and v_sales_rest */
	List<RequestForecastParameterSingle> getRequestForecastParameterSingleList(Integer requestId) throws DataServiceException ;
	/** making list of request elasticity parameters from v_elasticity_parameter and v_sales_rest */
	List<RequestElasticityParameterSingle> getRequestElasticityParameterSingleList(Integer requestId);
	/** making list of request forecast and elasticity parameters */
	List<RequestForecastAndElasticityParameterSingle> getRequestForecastAndElasticityParameterSingleList(Integer requestId);
	
	// ===========================Result files==================================================
	/** creating report with result of forecast (one whs and one sku)*/
	String createForecastResultFile(ResponceForecast responceForecast) throws DataServiceException;
	/** creating report with result of forecast (many whs and many sku)*/
	String createForecastMultipleResultFile(List<ResponceForecast> responceForecastList) throws DataServiceException;
	/** creating report with result of elasticity (many whs and many sku)*/
	String createElasticityMultipleResultFile(List<ResponceElasticity> elasticityResponseList) throws DataServiceException;
	/** creating report with result of forecast and elasticity together(many whs and many sku)*/
	String createForecastWithElasticityMultipleResultFile(List<ResponceForecastAndElasticity> forecastAndElasticityResponseList) throws DataServiceException;
	
	// ===========================Sending Emails==================================================
	/** getting v_request.email by v_request.request_id */
	String getEmailByRequestId(Long requestId);
	/** getting v_request.response_text by v_request.request_id */
    String getResponseTextByRequestId(Long requestId);
    /** creating web-link to v_request.attachment_path by v_request.request_id */
    String getAttachmentPathByRequestId(Long requestId);
    
    // ===========================Cleaning from old requests======================================
    /** getting v_request.attachment_path from v_request where response_date_time less or equal dateBound */
	List<String> getAttachmentPathListByResponseTimeBeforeMoment(Date dateBound);
	/** getting v_request.document_path from v_request where response_date_time less or equal dateBound */
	List<String> getDocumentPathListByResponseTimeBeforeMoment(Date dateBound);
	/** deleting records from v_request where response_date_time less or equal dateBound */
	Integer deleteRequestByResponseTimeBeforeMoment(Date dateBound);
}
