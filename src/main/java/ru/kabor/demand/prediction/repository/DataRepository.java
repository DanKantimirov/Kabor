package ru.kabor.demand.prediction.repository;

import java.util.List;

import org.springframework.jdbc.support.rowset.SqlRowSet;

import ru.kabor.demand.prediction.entity.RequestForecastParameterMultiple;
import ru.kabor.demand.prediction.entity.RequestForecastParameterSingle;
import ru.kabor.demand.prediction.entity.ResponceForecast;

public interface DataRepository {
	ResponceForecast getForecast(RequestForecastParameterSingle forecastParameters, SqlRowSet salesRowSet) throws Exception;
	List<ResponceForecast> getForecastMultiple(RequestForecastParameterMultiple forecastParameters, SqlRowSet salesRowSet) throws Exception;
	SqlRowSet getSales(RequestForecastParameterSingle forecastParameters);
	SqlRowSet getSalesMultiple(RequestForecastParameterMultiple forecastParameters);
	String getForecastFile(ResponceForecast responceForecast) throws Exception;
	String getForecastFileMultiple(List<ResponceForecast> responceForecastList) throws Exception;
    /** Get available document for processing*/
    String getAvailableDocument();
}
