package ru.kabor.demand.prediction.repository;

import java.util.List;

import org.springframework.jdbc.support.rowset.SqlRowSet;

import ru.kabor.demand.prediction.entity.RequestForecastParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponceForecast;
import ru.kabor.demand.prediction.service.DataServiceException;

public interface DataRepository {
	ResponceForecast getForecast(RequestForecastParameterSingle forecastParameters, SqlRowSet salesRowSet) throws DataServiceException;
	List<ResponceForecast> getForecastMultiple(RequestForecastParameterMultiple forecastParameters, SqlRowSet salesRowSet) throws DataServiceException;
	SqlRowSet getSales(RequestForecastParameterSingle forecastParameters) throws DataServiceException;
	SqlRowSet getSalesMultiple(RequestForecastParameterMultiple forecastParameters) throws DataServiceException;
	String getForecastFile(ResponceForecast responceForecast) throws DataServiceException;
	String getForecastFileMultiple(List<ResponceForecast> responceForecastList) throws DataServiceException;
    String getEmailByRequestId(Long requestId);
    String getResponseTextByRequestId(Long requestId);
    String getAttachmentPathByRequestId(Long requestId);
}
