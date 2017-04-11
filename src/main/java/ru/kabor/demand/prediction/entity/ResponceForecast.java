package ru.kabor.demand.prediction.entity;

import java.util.ArrayList;
import java.util.List;

/** That class describes result of making forecast for one shop and one product */
public class ResponceForecast {

	private Integer whsId;
	private Integer artId;
	private List<TimeMomentDescription> timeMomentsActual = new ArrayList<>();			//input information about time moments with calculated trand, seasonality and random
	private List<TimeMomentDescription> timeMomentsPrediction = new ArrayList<>();		//predicted time moments
	private Boolean hasError = false;													//is all right
	private String errorMessage = "";													//error message if not all right

	public ResponceForecast() {
		super();
	}

	public ResponceForecast(Integer whsId, Integer artId) {
		super();
		this.whsId = whsId;
		this.artId = artId;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
		if(this.errorMessage!=null && !this.errorMessage.trim().equals("")){
			this.hasError = true;
		} else{
			this.hasError = false;
		}
	}
	
	@Override
	public String toString() {
		return "ResponceForecast [whsId=" + whsId + ", artId=" + artId + ", hasError=" + hasError + ", errorMessage=" + errorMessage + "]";
	}

	public Integer getWhsId() {
		return whsId;
	}

	public void setWhsId(Integer whsId) {
		this.whsId = whsId;
	}

	public Integer getArtId() {
		return artId;
	}

	public void setArtId(Integer artId) {
		this.artId = artId;
	}

	public List<TimeMomentDescription> getTimeMomentsActual() {
		return timeMomentsActual;
	}

	public void setTimeMomentsActual(List<TimeMomentDescription> timeMomentsActual) {
		this.timeMomentsActual = timeMomentsActual;
	}

	public List<TimeMomentDescription> getTimeMomentsPrediction() {
		return timeMomentsPrediction;
	}

	public void setTimeMomentsPrediction(List<TimeMomentDescription> timeMomentsPrediction) {
		this.timeMomentsPrediction = timeMomentsPrediction;
	}

	public Boolean getHasError() {
		return hasError;
	}

	public void setHasError(Boolean hasError) {
		this.hasError = hasError;
	}
}
