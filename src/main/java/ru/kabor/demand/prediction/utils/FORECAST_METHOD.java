package ru.kabor.demand.prediction.utils;

public enum FORECAST_METHOD {
	AUTO_CHOOSE,
	ARIMA_AUTO,
	NEURAL_NETWORK,
	HOLT_WINTERS,
	ETS,
	TBATS;
}
