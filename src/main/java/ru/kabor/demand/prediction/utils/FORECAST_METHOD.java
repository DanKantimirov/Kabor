package ru.kabor.demand.prediction.utils;

/** It contains all forecast methods' names */
public enum FORECAST_METHOD {
	AUTO_CHOOSE,
	ARIMA_AUTO,
	NEURAL_NETWORK,
	HOLT_WINTERS,
	ETS,
	TBATS;
}
