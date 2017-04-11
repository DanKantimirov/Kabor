package ru.kabor.demand.prediction.utils;

/** Static variables used in project */
public class ConstantUtils {

	public static final int REQUEST_ADDED = 1;

	public static final int REQUEST_HOLDED_BY_DATA_IMPORT = 2;
	public static final int REQUEST_DATA_IMPORTED = 3;
	public static final int REQUEST_DATA_IMPORT_ERROR = -2;

	public static final int REQUEST_HOLDED_BY_FORECASTING = 4;
	public static final int REQUEST_FORECAST_COMPLITED = 5;
	public static final int REQUEST_FORECAST_ERROR = -4;
	
	public static final int REQUEST_HOLDED_BY_CALCULATING_ELASTICITY = 14;
	public static final int REQUEST_CALCULATING_ELASTICITY_COMPLETED = 15;
	public static final int REQUEST_CALCULATING_ELASTICITY_ERROR = -14;
	
	public static final int REQUEST_HOLDED_BY_FORECASTING_AND_ELASTICITY = 24;
	public static final int REQUEST_FORECAST_AND_ELASTICITY_COMPLETED = 25;
	public static final int REQUEST_FORECASTING_AND_ELASTICITY_ERROR = -24;

	public static final int PARSE_EXCEL_SALES_REST_LIST_SIZE = 500;
	
	public static final String REQUEST_TYPE_FORECAST = "FORECAST";
	public static final String REQUEST_TYPE_ELASTICITY = "ELASTICITY";
	public static final String REQUEST_TYPE_FORECASTANDELASTICITY = "FORECASTANDELASTICITY";

}
