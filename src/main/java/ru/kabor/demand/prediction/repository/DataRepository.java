package ru.kabor.demand.prediction.repository;

import java.util.Date;
import java.util.List;

import org.springframework.jdbc.support.rowset.SqlRowSet;

import ru.kabor.demand.prediction.entity.RequestElasticityParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestElasticityParameterSingle;
import ru.kabor.demand.prediction.entity.RequestForecastAndElasticityParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponseElasticity;
import ru.kabor.demand.prediction.entity.ResponseForecast;
import ru.kabor.demand.prediction.entity.ResponseForecastAndElasticity;
import ru.kabor.demand.prediction.service.DataServiceException;
import ru.kabor.demand.prediction.entity.RequestForecastAndElasticityParameterSingle;

/** It contains methods for working with database and calling rUtils */
public interface DataRepository {
	
	// ===========================Sales and Prices===========================================
	/** getting whs_id, art_id, sale_qnty, rest_qnty, day_id, price from v_sales_rest 
	 * @param forecastParameters parameters of forecast
	 * @return SqlRowSet
	 * @throws DataServiceException
	 */
	SqlRowSet getSalesWithPrices(RequestForecastParameterSingle forecastParameters) throws DataServiceException;
	
	
	/** getting whs_id, art_id, sale_qnty, rest_qnty, day_id, price from v_sales_rest
	 * @param forecastParameters parameters of forecast
	 * @return SqlRowSet
	 * @throws DataServiceException
	 */
	SqlRowSet getSalesMultipleWithPrices(RequestForecastParameterMultiple forecastParameters) throws DataServiceException;
	
	
	/** getting whs_id, art_id, sale_qnty, rest_qnty, day_id, price from v_sales_rest
	 * @param elasticityParameter parameters of elasticity 
	 * @return SqlRowSet
	 * @throws DataServiceException
	 */
	SqlRowSet getSalesWithPrices(RequestElasticityParameterSingle elasticityParameter) throws DataServiceException;
	
	/** getting whs_id, art_id, sale_qnty, rest_qnty, day_id, price from v_sales_rest 
	 * @param elasticityParameterMultiple parameters of elasticity 
	 * @return SqlRowSet
	 * @throws DataServiceException
	 */
	SqlRowSet getSalesMultipleWithPrices(RequestElasticityParameterMultiple elasticityParameterMultiple) throws DataServiceException;
	
	// ===========================Forecase and Elasticity for Excel mode=======================

	/** calling rUtils for getting forecast
	 * @param forecastParameters parameters of forecast
	 * @param salesRowSet sql rowSet with statistic
	 * @return
	 * @throws DataServiceException
	 */
	ResponseForecast getForecast(RequestForecastParameterSingle forecastParameters, SqlRowSet salesRowSet) throws DataServiceException;
	
	/** calling rUtils for calculating elasticity
	 * @param elasticityParameter parameters of elasticity
	 * @param salesRowSet sql rowSet with statistic
	 * @return
	 * @throws DataServiceException
	 */
	ResponseElasticity getElasticity(RequestElasticityParameterSingle elasticityParameter, SqlRowSet salesRowSet) throws DataServiceException;
	

	/** calling rUtils for getting forecast and calculating elasticity
	 * @param requestParameter parameters of forecast and elasticity
	 * @param salesRowSet sql rowSet with statistic
	 * @return
	 * @throws DataServiceException
	 */
	ResponseForecastAndElasticity getForecastAndElasticity(RequestForecastAndElasticityParameterSingle requestParameter, SqlRowSet salesRowSet) throws DataServiceException;
	
	// ===========================Forecase and Elasticity for Database mode=====================
	/** calling rUtils by MultithreadingForecastCallable for getting forecast
	 * @param forecastParameters parameters of forecast 
	 * @param salesRowSet sql rowSet with statistic
	 * @return
	 * @throws DataServiceException
	 */
	List<ResponseForecast> getForecastMultiple(RequestForecastParameterMultiple forecastParameters, SqlRowSet salesRowSet) throws DataServiceException;
	
	/** calling rUtils by MultithreadingElasticityCallable for getting forecast
	 * @param elasticityParameterMultiple parameters of elasticity 
	 * @param salesRowSet sql rowSet with statistic
	 * @return
	 * @throws DataServiceException
	 */
	List<ResponseElasticity> getElasticityMultiple(RequestElasticityParameterMultiple elasticityParameterMultiple, SqlRowSet salesRowSet) throws DataServiceException;
	
	
	/** calling rUtils by  MultithreadingForecastAndElasticityCallable for getting forecast
	 * @param forecastAndElasticityParameterMultiple parameters of forecast and elasticity 
	 * @param salesRowSet rowSet with statistic
	 * @return
	 * @throws DataServiceException 
	 */
	List<ResponseForecastAndElasticity> getForecastAndElasticityMultiple(RequestForecastAndElasticityParameterMultiple forecastAndElasticityParameterMultiple,
			SqlRowSet salesRowSet) throws DataServiceException;
	// ===========================Request parameters============================================
	/** making list of request forecast parameters from v_forecast_parameter and v_sales_rest
	 * @param requestId id of request
	 * @return list of forecast parameters
	 * @throws DataServiceException
	 */
	List<RequestForecastParameterSingle> getRequestForecastParameterSingleList(Integer requestId) throws DataServiceException ;
	
	/** making list of request elasticity parameters from v_elasticity_parameter and v_sales_rest
	 * @param requestId id of request
	 * @return list of elasticity parameters
	 */
	List<RequestElasticityParameterSingle> getRequestElasticityParameterSingleList(Integer requestId);
	
	/** making list of request forecast and elasticity parameters 
	 * @param requestId id of request
	 * @return list of forecast and elasticity parameters
	 */
	List<RequestForecastAndElasticityParameterSingle> getRequestForecastAndElasticityParameterSingleList(Integer requestId);
	
	// ===========================Result files==================================================
	
	/** creating report with result of forecast (one whs and one product)
	 * @param responseForecast response of forecast
	 * @return filename
	 * @throws DataServiceException
	 */
	String createForecastResultFile(ResponseForecast responseForecast) throws DataServiceException;
	
	/** creating report with result of forecast (many whs and many products)
	 * @param responseForecastList list of forecasts' responses
	 * @return filename
	 * @throws DataServiceException
	 */
	String createForecastMultipleResultFile(List<ResponseForecast> responseForecastList) throws DataServiceException;
	
	/** creating report with result of elasticity (many whs and many products)
	 * @param elasticityResponseList list of elastisityes' responses
	 * @return filename
	 * @throws DataServiceException
	 */
	String createElasticityMultipleResultFile(List<ResponseElasticity> elasticityResponseList) throws DataServiceException;
	
	/** creating report with result of forecast and elasticity together(many whs and many products)
	 * @param forecastAndElasticityResponseList list of forecasts' and elastisityes' responses
	 * @return filename
	 * @throws DataServiceException
	 */
	String createForecastWithElasticityMultipleResultFile(List<ResponseForecastAndElasticity> forecastAndElasticityResponseList) throws DataServiceException;
	
	// ===========================Sending Emails==================================================
	
	/** getting v_request.email by v_request.request_id 
	 * @param requestId id of request
	 * @return v_request.email
	 */
	String getEmailByRequestId(Long requestId);
	
    /** getting v_request.response_text by v_request.request_id 
     * @param requestId id of request
     * @return v_request.response_text
     */
    String getResponseTextByRequestId(Long requestId);
   
    /** creating web-link to v_request.attachment_path by v_request.request_id 
     * @param requestId id of request
     * @return http://..../v_request.request_id 
     */
    String getAttachmentPathByRequestId(Long requestId);
    
    // ===========================Cleaning from old requests======================================

	/** getting v_request.attachment_path from v_request where response_date_time less or equal dateBound
	 * @param dateBound moment of time
	 * @return v_request.attachment_path
	 */
	List<String> getAttachmentPathListByResponseTimeBeforeMoment(Date dateBound);
	

	/** getting v_request.document_path from v_request where response_date_time less or equal dateBound
	 * @param dateBound moment of time
	 * @return  v_request.document_path
	 */
	List<String> getDocumentPathListByResponseTimeBeforeMoment(Date dateBound);
	
	/** deleting records from v_request where response_date_time less or equal dateBound 
	 * @param dateBound moment of time
	 * @return count deleted records
	 */
	Integer deleteRequestByResponseTimeBeforeMoment(Date dateBound);

}
