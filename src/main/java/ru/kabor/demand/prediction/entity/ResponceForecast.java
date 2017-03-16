package ru.kabor.demand.prediction.entity;

import java.util.ArrayList;
import java.util.List;


public class ResponceForecast {

	private Integer whsId;
	private Integer artId;
	private List<TimeMomentDescription> timeMomentsActual = new ArrayList<>();
	private List<TimeMomentDescription> timeMomentsPrediction = new ArrayList<>();
	private List<TimeMomentDescription> timeMomentsSmoothed = new ArrayList<>();
	private Boolean hasError = false;
	private String errorMessage = "";

	public ResponceForecast() {
		super();
	}

	public ResponceForecast(Integer whsId, Integer artId) {
		super();
		this.whsId = whsId;
		this.artId = artId;
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

	public List<TimeMomentDescription> getTimeMomentsSmoothed() {
		return timeMomentsSmoothed;
	}

	public void setTimeMomentsSmoothed(List<TimeMomentDescription> timeMomentsSmoothed) {
		this.timeMomentsSmoothed = timeMomentsSmoothed;
	}
	
	

	public Boolean getHasError() {
		return hasError;
	}

	public void setHasError(Boolean hasError) {
		this.hasError = hasError;
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
	
}
